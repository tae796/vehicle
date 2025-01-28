package com.example.vehicle.exception.invalid;

import com.example.vehicle.exception.CustomException;
import com.example.vehicle.exception.ErrorCode;

public class NotFoundException extends CustomException {
    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }
}
