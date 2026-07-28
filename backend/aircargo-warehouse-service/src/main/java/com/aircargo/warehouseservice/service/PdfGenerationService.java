package com.aircargo.warehouseservice.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    static {
        XRLog.setLoggingEnabled(false);
    }

    public List<String> pdfPagesToDataUris(String base64PdfData) {
        List<String> result = new ArrayList<>();
        byte[] pdfBytes;
        try {
            pdfBytes = Base64.getDecoder().decode(base64PdfData);
        } catch (Exception e) {
            log.warn("Could not decode PDF base64 data: {}", e.getMessage());
            return result;
        }
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            if (pageCount > 20) pageCount = 20;
            for (int i = 0; i < pageCount; i++) {
                try {
                    BufferedImage image = renderer.renderImageWithDPI(i, 150, ImageType.RGB);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "JPEG", baos);
                    byte[] imageBytes = baos.toByteArray();
                    String b64 = Base64.getEncoder().encodeToString(imageBytes);
                    result.add("data:image/jpeg;base64," + b64);
                } catch (Exception e) {
                    log.warn("Could not render PDF page {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Could not load PDF document: {}", e.getMessage());
        }
        return result;
    }

    public byte[] generatePdf(String html) {
        List<File> tempFiles = new ArrayList<>();
        try {
            String processed = replaceDataUrisWithFiles(html, tempFiles);
            return renderPdf(processed);
        } finally {
            for (File f : tempFiles) {
                try { f.delete(); } catch (Exception ignored) {}
            }
        }
    }

    private String replaceDataUrisWithFiles(String html, List<File> tempFiles) {
        Pattern pattern = Pattern.compile("src=['\"]([^'\"]*data:[^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String dataUri = m.group(1);
            try {
                String base64Data = dataUri.substring(dataUri.indexOf(',') + 1);
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                String ext = dataUri.contains("png") ? ".png" : dataUri.contains("jpeg") || dataUri.contains("jpg") ? ".jpg" : ".png";
                File tempFile = Files.createTempFile("pdfimg_", ext).toFile();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(imageBytes);
                }
                tempFiles.add(tempFile);
                String filePath = tempFile.getAbsolutePath();
                if (File.separatorChar == '\\') {
                    filePath = filePath.replace("\\", "/");
                }
                m.appendReplacement(sb, "src='file:///" + filePath + "'");
            } catch (Exception e) {
                log.warn("Could not decode data URI ({} bytes): {}", dataUri.length(), e.getMessage());
                m.appendReplacement(sb, "src=''");
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static final File JETBRAINS_MONO_FONT = findFontFile();

    private static File findFontFile() {
        String[] paths = {
            "src/main/resources/fonts/JetBrainsMonoNerdFontMono-Regular.ttf",
            "fonts/JetBrainsMonoNerdFontMono-Regular.ttf",
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        try {
            java.net.URL res = PdfGenerationService.class.getResource("/fonts/JetBrainsMonoNerdFontMono-Regular.ttf");
            if (res != null) return new File(res.toURI());
        } catch (Exception ignored) {}
        return null;
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            if (JETBRAINS_MONO_FONT != null && JETBRAINS_MONO_FONT.exists()) {
                builder.useFont(JETBRAINS_MONO_FONT, "JetBrains Mono");
            }
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            byte[] pdf = os.toByteArray();
            if (pdf.length == 0) {
                throw new RuntimeException("PDF generado vacío");
            }
            return pdf;
        } catch (Exception e) {
            log.error("Error generando PDF ({} chars HTML): {}", html.length(), e.getMessage(), e);
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }
}
