package com.aircargo.mawbservice.service;

import com.aircargo.mawbservice.dto.DuaRecordDTO;
import com.aircargo.mawbservice.entity.DuaRecord;
import com.aircargo.mawbservice.entity.DuaStatus;
import com.aircargo.mawbservice.repository.DuaRecordRepository;
import com.aircargo.mawbservice.repository.MawbRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DuaRecordService {

    private final DuaRecordRepository repository;
    private final MawbRepository mawbRepository;

    public DuaRecordService(DuaRecordRepository repository, MawbRepository mawbRepository) {
        this.repository = repository;
        this.mawbRepository = mawbRepository;
    }

    @Transactional(readOnly = true)
    public List<DuaRecordDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(DuaRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DuaRecordDTO> getByMawb(UUID mawbId) {
        return repository.findByMawbIdOrderByCreatedAtDesc(mawbId).stream()
                .map(DuaRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "dua-records", key = "#id")
    public DuaRecordDTO getById(UUID id) {
        return repository.findById(id)
                .map(DuaRecordDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("DUA no encontrado: " + id));
    }

    @Transactional
    @CacheEvict(value = "dua-records", allEntries = true)
    public DuaRecordDTO create(DuaRecordDTO dto) {
        mawbRepository.findById(dto.getMawbId())
                .orElseThrow(() -> new IllegalArgumentException("MAWB no encontrado: " + dto.getMawbId()));
        DuaRecord entity = new DuaRecord();
        entity.setMawbId(dto.getMawbId());
        entity.setDuaNumber(dto.getDuaNumber());
        entity.setDocumentUrl(dto.getDocumentUrl());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : DuaStatus.PENDING);
        entity.setDuaDate(dto.getDuaDate());
        entity.setNotes(dto.getNotes());
        entity.setCustomsBroker(dto.getCustomsBroker());
        DuaRecord saved = repository.save(entity);
        return DuaRecordDTO.fromEntity(saved);
    }

    @Transactional
    @CacheEvict(value = "dua-records", allEntries = true)
    public DuaRecordDTO update(UUID id, DuaRecordDTO dto) {
        return repository.findById(id).map(entity -> {
            entity.setDuaNumber(dto.getDuaNumber());
            if (dto.getDocumentUrl() != null) entity.setDocumentUrl(dto.getDocumentUrl());
            entity.setStatus(dto.getStatus());
            entity.setDuaDate(dto.getDuaDate());
            entity.setNotes(dto.getNotes());
            entity.setCustomsBroker(dto.getCustomsBroker());
            DuaRecord saved = repository.save(entity);
            return DuaRecordDTO.fromEntity(saved);
        }).orElseThrow(() -> new IllegalArgumentException("DUA no encontrado: " + id));
    }

    @Transactional
    @CacheEvict(value = "dua-records", allEntries = true)
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("DUA no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
