package com.example.vehicle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStatusRequestDto {


    private Long id; // 차량 Id
    private String status; // 수정할 상태



}
