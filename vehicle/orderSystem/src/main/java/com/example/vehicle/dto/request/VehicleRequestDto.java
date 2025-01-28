package com.example.vehicle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequestDto {

    private String brand;
    private String model;
    private Integer year;
    private String status;
    private List<String> categories;
}
