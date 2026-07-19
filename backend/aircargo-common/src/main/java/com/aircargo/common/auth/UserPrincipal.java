package com.aircargo.common.auth;

import java.util.UUID;

public record UserPrincipal(String userId, String role, String airlineId, String email, String fullName) {
    public UUID getUserIdAsUuid() {
        if (userId == null || userId.isBlank()) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public UUID getAirlineIdAsUuid() {
        if (airlineId == null || airlineId.isBlank()) return null;
        try {
            return UUID.fromString(airlineId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
