package com.example.vehicle.exception.invalid;

import com.example.vehicle.exception.CustomException;
import com.example.vehicle.exception.ErrorCode;

public class SerializeException extends CustomException {
    public SerializeException() {
        super(ErrorCode.SERIALIZE_ERROR);
    }
}
