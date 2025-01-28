package com.example.vehicle.controller;

import com.example.vehicle.dto.request.UpdateCategoryRequestDto;
import com.example.vehicle.dto.request.UpdateStatusRequestDto;
import com.example.vehicle.dto.request.VehicleRequestDto;
import com.example.vehicle.dto.response.ResponseDto;
import com.example.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
public class VehicleController {

    @Autowired
    private VehicleService redisService;

    // brand, model, year로 데이터 가져오기
    @GetMapping
    public ResponseDto getVehicleWithCategories(@RequestParam String brand,
                                                           @RequestParam String model,
                                                           @RequestParam Integer year) {
        return ResponseDto.ok(redisService.getVehicleWithCategories(brand, model, year));
    }

    @PostMapping
    public ResponseDto createVehicle(@RequestBody VehicleRequestDto vehicleRequestDto) {
        return ResponseDto.ok(redisService.saveVehicle(vehicleRequestDto));
    }

    @GetMapping("/status/{status}")
    public ResponseDto getVehiclesByStatus(@PathVariable String status) {
        return ResponseDto.ok(redisService.getVehiclesByStatus(status));
    }

    @GetMapping("/category/{categoryName}")
    public ResponseDto getVehiclesByCategory(@PathVariable String categoryName) {
        return ResponseDto.ok(redisService.getVehiclesByCategory(categoryName));
    }

    @PutMapping("/status")
    public ResponseDto updateStatus(@RequestBody UpdateStatusRequestDto request) {
        return ResponseDto.ok(redisService.updateStatus(request.getId(), request.getStatus()));
    }

    @PutMapping("/category")
    public ResponseDto updateCategory(@RequestBody UpdateCategoryRequestDto request) {
        return ResponseDto.ok(redisService.updateCategories(request.getId(), request.getCategories()));
    }
}