package com.aircargo.feign.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private UUID airlineId;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private Boolean mfaEnabled;
    private OffsetDateTime lastLogin;
    private List<UUID> siteIds;

    public UserDTO() {}

    public UserDTO(UUID id, String email, String fullName, String role, UUID airlineId,
                   Boolean isActive, Boolean mustChangePassword, Boolean mfaEnabled,
                   OffsetDateTime lastLogin, List<UUID> siteIds) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.airlineId = airlineId;
        this.isActive = isActive;
        this.mustChangePassword = mustChangePassword;
        this.mfaEnabled = mfaEnabled;
        this.lastLogin = lastLogin;
        this.siteIds = siteIds;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UUID getAirlineId() { return airlineId; }
    public void setAirlineId(UUID airlineId) { this.airlineId = airlineId; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Boolean getMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(Boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
    public OffsetDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(OffsetDateTime lastLogin) { this.lastLogin = lastLogin; }
    public List<UUID> getSiteIds() { return siteIds; }
    public void setSiteIds(List<UUID> siteIds) { this.siteIds = siteIds; }
}
