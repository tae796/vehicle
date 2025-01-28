package com.example.vehicle.exception;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String message;
    private final int status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("errors")
    private List<CustomError> customErrors;

    @Builder
    public ErrorResponse(String message, int status, Errors errors) {
        this.message = message;
        this.status = status;
        this.customErrors = new ArrayList<>();
        if (errors != null) {
            errors.getFieldErrors().forEach(error -> this.customErrors.add(
                    new CustomFieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage())));
        }
    }

    public static class CustomError {

    }

    @Getter
    public static class CustomFieldError extends CustomError{

        private final String field;
        private final Object value;
        private final String reason;

        public CustomFieldError(String field, Object value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

    }

}
