package com.aircargo.exportservice.repository;

import com.aircargo.exportservice.entity.AirlineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AirlineRepository extends JpaRepository<AirlineEntity, UUID> {
}
