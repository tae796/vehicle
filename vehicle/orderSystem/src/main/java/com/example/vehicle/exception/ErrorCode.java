package com.example.vehicle.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //InvalidError
    NOT_FOUND(400, "정보가 존재하지 않습니다."),
    DUPLICATE_VEHICLE(400, "차량 정보가 중복됩니다. "),
    SERIALIZE_ERROR(400, "Serialize에서 문제가 생겼습니다"),
    DESERIALIZE_ERROR(400, "Deserialize에서 문제가 생겼습니다"),
    INVALID_REQUEST_FORMANT(400,"입력 정보가 잘못됐습니다."),
    INVALID_REQUEST_PART(400, "입력 정보가 올바르지 않습니다.");

    private final String message;
    private final int status;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }

}
