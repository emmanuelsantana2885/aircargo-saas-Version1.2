package com.aircargo.mawbservice.repository;

import com.aircargo.mawbservice.entity.LabelTemplate;
import com.aircargo.mawbservice.entity.LabelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabelTemplateRepository extends JpaRepository<LabelTemplate, UUID> {

    List<LabelTemplate> findAllByTypeOrderByUpdatedAtDesc(LabelType type);

    Optional<LabelTemplate> findFirstByTypeAndIsDefaultTrue(LabelType type);
}
