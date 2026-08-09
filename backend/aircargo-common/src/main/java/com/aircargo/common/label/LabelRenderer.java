package com.aircargo.common.label;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.Base64;

/**
 * Shared label renderer: turns a JSON label template (elements in millimetres)
 * plus resolved data maps into ZPL (Zebra) or PDF (openhtmltopdf) output.
 */
@Component
public class LabelRenderer {

    private static final double MM_PER_INCH = 25.4;

    private final ObjectMapper objectMapper;

    public LabelRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public String renderZpl(LabelSpec spec, List<Map<String, String>> dataList, int quantity) {
        double labelW = spec.widthInches;
        double labelH = spec.heightInches;
        if ("VERTICAL".equalsIgnoreCase(spec.orientation)) {
            double tmp = labelW;
            labelW = labelH;
            labelH = tmp;
        }
        int dpi = spec.dpi > 0 ? spec.dpi : 203;
        List<Element> elements = parseElements(spec.configJson);

        int pw = mmToDots(labelW * MM_PER_INCH, dpi);
        int ll = mmToDots(labelH * MM_PER_INCH, dpi);

        StringBuilder sb = new StringBuilder();
        sb.append("^XA")
          .append("^PW").append(pw)
          .append("^LL").append(ll)
          .append("^CI28")
          .append("^BY2,2.0,3");

        int q = Math.max(1, quantity);
        for (Map<String, String> data : dataList) {
            for (int copy = 0; copy < q; copy++) {
                for (Element el : elements) {
                    renderElementZpl(sb, el, data, dpi);
                }
                sb.append("^XZ\n^XA")
                  .append("^PW").append(pw)
                  .append("^LL").append(ll)
                  .append("^CI28")
                  .append("^BY2,2.0,3");
            }
        }
        String out = sb.toString();
        int tail = out.lastIndexOf("^XZ\n^XA");
        if (tail >= 0) {
            out = out.substring(0, tail + 4) + "\n";
        } else {
            out += "^XZ\n";
        }
        return out;
    }

    public byte[] renderPdf(LabelSpec spec, List<Map<String, String>> dataList, int quantity) throws IOException {
        double labelW = spec.widthInches;
        double labelH = spec.heightInches;
        if ("VERTICAL".equalsIgnoreCase(spec.orientation)) {
            double tmp = labelW;
            labelW = labelH;
            labelH = tmp;
        }
        List<Element> elements = parseElements(spec.configJson);

        String html = buildPdfHtml(labelW, labelH, elements, dataList, Math.max(1, quantity));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("PDF render failed", e);
        }
    }

    // ------------------------------------------------------------------
    // Config parsing
    // ------------------------------------------------------------------

    public static class LabelSpec {
        public double widthInches;
        public double heightInches;
        public String orientation = "HORIZONTAL"; // HORIZONTAL | VERTICAL (swaps W/H)
        public int dpi = 203;
        public String configJson = "{\"elements\":[]}";
    }

    public static class Element {
        public String id;
        public String type = "text";       // text | barcode | qrcode | line | rect
        public double x;                    // mm
        public double y;                    // mm
        public double w;                    // mm
        public double h;                    // mm
        public double fontSize = 6.0;       // mm
        public boolean bold;
        public String align = "left";       // left | center | right
        public String dataSource = "TEXT";  // placeholder key or TEXT
        public String text = "";            // static text when dataSource == TEXT
        public String barcodeFormat = "CODE128"; // CODE128 | QR
        public double barcodeHeight = 15;   // mm (PDF)
    }

    public List<Element> parseElements(String configJson) {
        List<Element> out = new ArrayList<>();
        if (configJson == null || configJson.isBlank()) return out;
        try {
            JsonNode root = objectMapper.readTree(configJson);
            JsonNode arr = root.has("elements") ? root.get("elements") : root;
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    Element el = new Element();
                    el.id = node.path("id").asText(UUID.randomUUID().toString().substring(0, 8));
                    el.type = node.path("type").asText("text");
                    el.x = node.path("x").asDouble(0);
                    el.y = node.path("y").asDouble(0);
                    el.w = node.path("w").asDouble(0);
                    el.h = node.path("h").asDouble(0);
                    el.fontSize = node.path("fontSize").asDouble(6.0);
                    el.bold = node.path("bold").asBoolean(false);
                    el.align = node.path("align").asText("left");
                    el.dataSource = node.path("dataSource").asText("TEXT");
                    el.text = node.path("text").asText("");
                    el.barcodeFormat = node.path("barcodeFormat").asText("CODE128");
                    el.barcodeHeight = node.path("barcodeHeight").asDouble(15);
                    out.add(el);
                }
            }
        } catch (Exception ignored) { }
        return out;
    }

    // ------------------------------------------------------------------
    // ZPL
    // ------------------------------------------------------------------

    private void renderElementZpl(StringBuilder sb, Element el, Map<String, String> data, int dpi) {
        int x = mmToDots(el.x, dpi);
        int y = mmToDots(el.y, dpi);
        String value = resolve(el, data);

        switch (el.type) {
            case "barcode" -> {
                int h = mmToDots(el.barcodeHeight > 0 ? el.barcodeHeight : el.h, dpi);
                if ("QR".equalsIgnoreCase(el.barcodeFormat)) {
                    int mag = Math.max(1, mmToDots(el.h, dpi) / 28);
                    sb.append("^FO").append(x).append(',').append(y)
                      .append("^BQN,2,").append(mag)
                      .append("^FDQA,").append(zplEscape(value)).append("^FS");
                } else {
                    sb.append("^FO").append(x).append(',').append(y)
                      .append("^BCN,").append(h).append(",Y,N,N,N")
                      .append("^FD").append(zplEscape(value)).append("^FS");
                }
            }
            case "qrcode" -> {
                int mag = Math.max(1, mmToDots(el.h, dpi) / 28);
                sb.append("^FO").append(x).append(',').append(y)
                  .append("^BQN,2,").append(mag)
                  .append("^FDQA,").append(zplEscape(value)).append("^FS");
            }
            case "line", "rect" -> {
                int w = Math.max(1, mmToDots(el.w, dpi));
                int h = Math.max(1, mmToDots(el.h, dpi));
                int thick = Math.max(1, mmToDots(Math.min(1.5, Math.max(0.1, el.h)), dpi));
                sb.append("^FO").append(x).append(',').append(y)
                  .append("^GB").append(w).append(',').append(h).append(',').append(thick).append(",B,0^FS");
            }
            default -> {
                int fh = mmToDots(el.fontSize, dpi);
                int fw = Math.max(1, fh / 2);
                int bw = Math.max(1, mmToDots(el.w, dpi));
                int alignChar = "center".equalsIgnoreCase(el.align) ? 1 : "right".equalsIgnoreCase(el.align) ? 2 : 0;
                sb.append("^FO").append(x).append(',').append(y)
                  .append("^A0N,").append(fh).append(',').append(fw)
                  .append("^FB").append(bw).append(",1,0,").append(alignChar)
                  .append("^FD").append(zplEscape(value)).append("^FS");
            }
        }
    }

    private String resolve(Element el, Map<String, String> data) {
        String v = "TEXT".equalsIgnoreCase(el.dataSource)
                ? el.text
                : (data != null ? data.getOrDefault(el.dataSource, "") : "");
        return v == null ? "" : v;
    }

    private static String zplEscape(String s) {
        if (s == null) return "";
        return s.replace("_", "_5F").replace("^", "_5E").replace("~", "_7E");
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    private String buildPdfHtml(double labelW, double labelH, List<Element> elements,
                                List<Map<String, String>> dataList, int quantity) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>")
          .append("html,body{margin:0;padding:0;}")
          .append("body{font-family:Helvetica,Arial,sans-serif;}")
          .append("@page{size:").append(round2(labelW * MM_PER_INCH)).append("mm ")
          .append(round2(labelH * MM_PER_INCH)).append("mm;margin:0}")
          .append(".label{position:relative;width:").append(round2(labelW * MM_PER_INCH)).append("mm;")
          .append("height:").append(round2(labelH * MM_PER_INCH)).append("mm;page-break-after:always;")
          .append("box-sizing:border-box;}")
          .append(".el{position:absolute;box-sizing:border-box;overflow:hidden;white-space:nowrap;}")
          .append(".el-text{font-family:Helvetica,Arial,sans-serif;}")
          .append("</style></head><body>");

        for (Map<String, String> data : dataList) {
            for (int copy = 0; copy < quantity; copy++) {
                sb.append("<div class='label'>");
                for (Element el : elements) {
                    sb.append(renderElementHtml(el, data));
                }
                sb.append("</div>");
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String renderElementHtml(Element el, Map<String, String> data) {
        String value = resolve(el, data);
        String left = round2(el.x);
        String top = round2(el.y);
        switch (el.type) {
            case "barcode", "qrcode" -> {
                try {
                    boolean qr = "QR".equalsIgnoreCase(el.barcodeFormat);
                    byte[] png = renderBarcodePng(value, qr ? "QR" : "CODE128",
                            Math.max(24, (int) (el.w * 10)), Math.max(24, (int) (el.barcodeHeight * 10)));
                    String b64 = Base64.getEncoder().encodeToString(png);
                    return "<img class='el' style='left:" + left + "mm;top:" + top + "mm;width:"
                            + round2(el.w) + "mm;height:" + round2(el.barcodeHeight) + "mm;' src=\"data:image/png;base64," + b64 + "\"/>";
                } catch (Exception e) {
                    return "";
                }
            }
            case "line", "rect" -> {
                String borderW = Math.max(0.25, Math.min(1.5, Math.max(0.1, el.h))) + "mm";
                return "<div class='el' style='left:" + left + "mm;top:" + top + "mm;width:"
                        + round2(el.w) + "mm;height:" + round2(el.h) + "mm;border:"
                        + (el.type.equals("line") ? "0.4mm solid #000" : borderW + " solid #000") + "'>";
            }
            default -> {
                String align = "left".equalsIgnoreCase(el.align) ? "left" : "center".equalsIgnoreCase(el.align) ? "center" : "right";
                String bold = el.bold ? "bold" : "normal";
                String fsz = round2(el.fontSize * 2.834);
                return "<div class='el el-text' style='left:" + left + "mm;top:" + top + "mm;width:"
                        + round2(el.w) + "mm;font-size:" + fsz + "pt;font-weight:" + bold + ";text-align:" + align
                        + ";'>" + htmlEscape(value) + "</div>";
            }
        }
    }

    private byte[] renderBarcodePng(String data, String format, int widthPx, int heightPx) throws Exception {
        if (data == null || data.isBlank()) data = "EMPTY";
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BufferedImage out;
        if ("QR".equals(format)) {
            Writer writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, widthPx, heightPx, hints);
            out = toImage(matrix, widthPx, heightPx);
        } else {
            Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.CODE_128, 0, 0, hints);
            BufferedImage moduleImg = toImage(matrix, matrix.getWidth(), matrix.getHeight());
            out = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, widthPx, heightPx);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(moduleImg, 0, 0, widthPx, heightPx, null);
            g.dispose();
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(out, "png", baos);
            return baos.toByteArray();
        }
    }

    private static BufferedImage toImage(BitMatrix matrix, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? 0 : 0xFFFFFF);
            }
        }
        return img;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static int mmToDots(double mm, int dpi) {
        return Math.max(1, (int) Math.round(mm / MM_PER_INCH * dpi));
    }

    private static String round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
