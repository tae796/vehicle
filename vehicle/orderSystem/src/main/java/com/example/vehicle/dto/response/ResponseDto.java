package com.example.vehicle.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto {

    private int status = 200;
    private String message = "ok";
    private LocalDateTime timestamp = LocalDateTime.now();
    private Object body;

    private ResponseDto(Object data) {
        this.body = data;
    }

    public static ResponseDto ok() {
        return new ResponseDto();
    }

    public static ResponseDto ok(Object data) {
        return new ResponseDto(data);
    }

}