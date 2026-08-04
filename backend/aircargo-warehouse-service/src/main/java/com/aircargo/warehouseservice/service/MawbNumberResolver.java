package com.aircargo.warehouseservice.service;

import com.aircargo.feign.client.MawbClient;
import com.aircargo.feign.dto.MawbDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the MAWB (AWB) number for a warehouse receipt.
 * Priority: stored value → mawb-service via Feign → direct read of the shared
 * mawb table (all services share one PostgreSQL database).
 * Best-effort: never throws, returns null when unresolvable.
 */
@Component
public class MawbNumberResolver {

    private static final Logger log = LoggerFactory.getLogger(MawbNumberResolver.class);

    private final MawbClient mawbClient;
    private final JdbcTemplate jdbcTemplate;

    public MawbNumberResolver(MawbClient mawbClient, JdbcTemplate jdbcTemplate) {
        this.mawbClient = mawbClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String resolve(UUID mawbId, String storedMawbNumber) {
        if (storedMawbNumber != null && !storedMawbNumber.isBlank()) {
            return storedMawbNumber;
        }
        if (mawbId == null) {
            return null;
        }

        String viaFeign = resolveViaFeign(mawbId);
        if (viaFeign != null) {
            return viaFeign;
        }

        return resolveViaSharedDb(mawbId);
    }

    private String resolveViaFeign(UUID mawbId) {
        try {
            MawbDTO dto = mawbClient.getMawbById(mawbId);
            if (dto != null && dto.getAwbNumber() != null && !dto.getAwbNumber().isBlank()) {
                return dto.getAwbNumber();
            }
        } catch (Exception e) {
            log.debug("Feign lookup of MAWB {} failed, trying shared-DB read", mawbId);
        }
        return null;
    }

    private String resolveViaSharedDb(UUID mawbId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT awb_number FROM mawb WHERE id = ?", String.class, mawbId);
        } catch (Exception e) {
            log.warn("Shared-DB lookup of MAWB {} failed: {}", mawbId, e.getMessage());
            return null;
        }
    }
}
