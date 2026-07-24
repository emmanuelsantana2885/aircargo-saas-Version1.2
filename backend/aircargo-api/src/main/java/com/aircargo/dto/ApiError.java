package com.aircargo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private OffsetDateTime timestamp;
    private Map<String, String> fieldErrors;

    public static ApiError of(int status, String error, String message, String path) {
        ApiError e = new ApiError();
        e.setStatus(status);
        e.setError(error);
        e.setMessage(message);
        e.setPath(path);
        e.setTimestamp(OffsetDateTime.now());
        return e;
    }
}
