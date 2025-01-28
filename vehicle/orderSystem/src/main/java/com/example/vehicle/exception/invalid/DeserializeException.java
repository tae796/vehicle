package com.example.vehicle.exception.invalid;

import com.example.vehicle.exception.CustomException;
import com.example.vehicle.exception.ErrorCode;

public class DeserializeException extends CustomException {
    public DeserializeException() {
        super(ErrorCode.DESERIALIZE_ERROR);
    }
}
