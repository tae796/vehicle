package com.example.vehicle.exception;

import lombok.Getter;
import java.io.Serial;

@Getter
public class CustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    /**
     * 상위 예외 없이 바로 예외를 던질 때 사용
     * @param errorCode
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
