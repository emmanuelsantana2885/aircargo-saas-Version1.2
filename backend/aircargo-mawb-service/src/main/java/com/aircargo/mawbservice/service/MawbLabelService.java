package com.aircargo.mawbservice.service;

import com.aircargo.common.label.LabelRenderer;
import com.aircargo.mawbservice.dto.LabelPrintRequest;
import com.aircargo.mawbservice.entity.LabelTemplate;
import com.aircargo.mawbservice.entity.LabelType;
import com.aircargo.mawbservice.entity.Mawb;
import com.aircargo.mawbservice.repository.MawbRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MawbLabelService {

    private final MawbRepository mawbRepository;
    private final LabelTemplateService templateService;
    private final LabelRenderer renderer;

    public MawbLabelService(MawbRepository mawbRepository,
                            LabelTemplateService templateService,
                            LabelRenderer renderer) {
        this.mawbRepository = mawbRepository;
        this.templateService = templateService;
        this.renderer = renderer;
    }

    private LabelRenderer.LabelSpec toSpec(LabelTemplate template) {
        LabelRenderer.LabelSpec spec = new LabelRenderer.LabelSpec();
        spec.widthInches = template.getWidthInches().doubleValue();
        spec.heightInches = template.getHeightInches().doubleValue();
        spec.orientation = template.getOrientation() != null ? template.getOrientation() : "HORIZONTAL";
        spec.dpi = template.getDpi() != null ? template.getDpi() : 203;
        spec.configJson = template.getConfigJson();
        return spec;
    }

    public LabelTemplate resolveTemplate(UUID templateId) {
        if (templateId != null) {
            return templateService.getEntityById(templateId)
                    .filter(t -> t.getType() == LabelType.CARGO)
                    .orElseThrow(() -> new IllegalArgumentException("Plantilla CARGO no encontrada: " + templateId));
        }
        return templateService.getDefault(LabelType.CARGO)
                .flatMap(dto -> templateService.getEntityById(dto.getId()))
                .orElseThrow(() -> new IllegalArgumentException("No hay plantilla de etiqueta CARGO configurada"));
    }

    @Transactional(readOnly = true)
    public byte[] renderPdf(LabelPrintRequest request) {
        LabelTemplate template = resolveTemplate(request.getTemplateId());
        List<Map<String, String>> dataList = buildDataList(request);
        try {
            return renderer.renderPdf(toSpec(template), dataList, request.getQuantity() != null ? request.getQuantity() : 1);
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de etiquetas: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String renderZpl(LabelPrintRequest request) {
        LabelTemplate template = resolveTemplate(request.getTemplateId());
        List<Map<String, String>> dataList = buildDataList(request);
        return renderer.renderZpl(toSpec(template), dataList, request.getQuantity() != null ? request.getQuantity() : 1);
    }

    private List<Map<String, String>> buildDataList(LabelPrintRequest request) {
        List<Map<String, String>> dataList = new ArrayList<>();
        if (request.getIds() == null) return dataList;
        for (UUID id : request.getIds()) {
            Mawb mawb = mawbRepository.findById(id).orElse(null);
            if (mawb == null) continue;
            Map<String, String> data = new HashMap<>();
            data.put("AWB_NUMBER", nvl(mawb.getAwbNumber()));
            data.put("SHIPPER_NAME", nvl(mawb.getShipperName()));
            data.put("CONSIGNEE_NAME", nvl(mawb.getConsigneeName()));
            data.put("ORIGIN", nvl(mawb.getOrigin()));
            data.put("DESTINATION", nvl(mawb.getDestination()));
            data.put("PIECES", mawb.getPieces() != null ? String.valueOf(mawb.getPieces()) : "");
            data.put("WEIGHT_KG", mawb.getReportedWeightKg() != null ? mawb.getReportedWeightKg().stripTrailingZeros().toPlainString() : "");
            data.put("CHARGEABLE_KG", mawb.getChargeableWeightKg() != null ? mawb.getChargeableWeightKg().stripTrailingZeros().toPlainString() : "");
            data.put("COMMODITY", mawb.getCommodityType() != null ? mawb.getCommodityType().name() : "");
            data.put("STATUS", mawb.getStatus() != null ? mawb.getStatus().name() : "");
            if (request.getOverrides() != null) {
                Map<String, String> ov = request.getOverrides().get(id.toString());
                if (ov != null) data.putAll(ov);
            }
            dataList.add(data);
        }
        return dataList;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
