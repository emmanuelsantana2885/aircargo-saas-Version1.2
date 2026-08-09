package com.aircargo.mawbservice.service;

import com.aircargo.mawbservice.dto.LabelTemplateDTO;
import com.aircargo.mawbservice.entity.LabelTemplate;
import com.aircargo.mawbservice.entity.LabelType;
import com.aircargo.mawbservice.repository.LabelTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LabelTemplateService {

    private final LabelTemplateRepository repository;

    public LabelTemplateService(LabelTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<LabelTemplateDTO> getAll(LabelType type) {
        return repository.findAllByTypeOrderByUpdatedAtDesc(type).stream()
                .map(LabelTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<LabelTemplateDTO> getById(UUID id) {
        return repository.findById(id).map(LabelTemplateDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<LabelTemplateDTO> getDefault(LabelType type) {
        return repository.findFirstByTypeAndIsDefaultTrue(type).map(LabelTemplateDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<LabelTemplate> getEntityById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public LabelTemplateDTO create(LabelTemplateDTO dto) {
        LabelTemplate template = LabelTemplateDTO.toEntity(dto);
        LabelTemplate saved = repository.save(template);
        maybeClearDefault(saved);
        return LabelTemplateDTO.fromEntity(saved);
    }

    @Transactional
    public Optional<LabelTemplateDTO> update(UUID id, LabelTemplateDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    if (dto.getName() != null) existing.setName(dto.getName());
                    if (dto.getWidthInches() != null) existing.setWidthInches(dto.getWidthInches());
                    if (dto.getHeightInches() != null) existing.setHeightInches(dto.getHeightInches());
                    if (dto.getOrientation() != null) existing.setOrientation(dto.getOrientation());
                    if (dto.getDpi() != null) existing.setDpi(dto.getDpi());
                    if (dto.getConfigJson() != null) existing.setConfigJson(dto.getConfigJson());
                    if (dto.getIsDefault() != null) existing.setIsDefault(dto.getIsDefault());
                    LabelTemplate saved = repository.save(existing);
                    maybeClearDefault(saved);
                    return LabelTemplateDTO.fromEntity(saved);
                });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    private void maybeClearDefault(LabelTemplate saved) {
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            List<LabelTemplate> others = repository.findAllByTypeOrderByUpdatedAtDesc(saved.getType());
            for (LabelTemplate t : others) {
                if (!t.getId().equals(saved.getId()) && Boolean.TRUE.equals(t.getIsDefault())) {
                    t.setIsDefault(false);
                    repository.save(t);
                }
            }
        }
    }
}
