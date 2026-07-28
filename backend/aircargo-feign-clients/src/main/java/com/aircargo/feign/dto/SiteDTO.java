package com.aircargo.feign.dto;

import java.util.UUID;

public class SiteDTO {
    private UUID id;
    private String code;
    private String name;
    private String country;
    private Boolean isActive;

    public SiteDTO() {}

    public SiteDTO(UUID id, String code, String name, String country, Boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.country = country;
        this.isActive = isActive;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
