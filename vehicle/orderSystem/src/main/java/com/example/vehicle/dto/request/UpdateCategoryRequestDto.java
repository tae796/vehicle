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
public class UpdateCategoryRequestDto {


    private Long id; // 차량 Id
    private List<String> categories; // 수정할 상태



}
