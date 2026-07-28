package com.aircargo.exportservice.repository;

import com.aircargo.exportservice.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
}
