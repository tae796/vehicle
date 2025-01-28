package com.example.vehicle.exception.invalid;

import com.example.vehicle.exception.CustomException;
import com.example.vehicle.exception.ErrorCode;

public class DuplicateVehicleException extends CustomException {
    public DuplicateVehicleException() {
        super(ErrorCode.DUPLICATE_VEHICLE);
    }
}
