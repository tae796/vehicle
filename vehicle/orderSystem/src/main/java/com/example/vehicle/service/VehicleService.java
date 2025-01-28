package com.example.vehicle.service;

import com.example.vehicle.dto.request.VehicleRequestDto;
import com.example.vehicle.dto.response.VehicleResponseDto;
import com.example.vehicle.entity.Category;
import com.example.vehicle.entity.Vehicle;
import com.example.vehicle.entity.VehicleCategory;
import com.example.vehicle.exception.invalid.DeserializeException;
import com.example.vehicle.exception.invalid.DuplicateVehicleException;
import com.example.vehicle.exception.invalid.NotFoundException;
import com.example.vehicle.exception.invalid.SerializeException;
import com.example.vehicle.repository.CategoryRepository;
import com.example.vehicle.repository.VehicleCategoryRepository;
import com.example.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private static final long CACHE_TTL = 3600; // 1시간 (3600초)


    // 3개 조건으로 차량 조회
    public VehicleResponseDto getVehicleWithCategories(String brand, String model, Integer year) {

        // redis 조회
        String redisKey = "vehicle:" + brand + ":" + model + ":" + year;

        VehicleResponseDto cachedDto = (VehicleResponseDto) redisTemplate.opsForValue().get(redisKey);
        if (cachedDto != null) {
            return cachedDto; // 캐시된 데이터 반환
        }

        // 차량 조회
        Vehicle vehicle = vehicleRepository.findByBrandModelYear(brand, model, year)
                .orElseThrow(NotFoundException::new);

        // 차량 id로 카테고리 조회
        List<String> categories = vehicleCategoryRepository.findCategoryNamesByVehicleId(vehicle.getId());


        // vehicledto 생성 및 redis에 저장
        VehicleResponseDto vehicleResponseDto = new VehicleResponseDto();
        vehicleResponseDto.setId(vehicle.getId());
        vehicleResponseDto.setBrand(vehicle.getBrand());
        vehicleResponseDto.setModel(vehicle.getModel());
        vehicleResponseDto.setYear(vehicle.getYear());
        vehicleResponseDto.setStatus(vehicle.getStatus());
        vehicleResponseDto.setCategories(categories);

        redisTemplate.opsForValue().set(redisKey, vehicleResponseDto, CACHE_TTL, TimeUnit.SECONDS);

        return vehicleResponseDto;
    }


    // 차량정보등록
    public VehicleResponseDto saveVehicle(VehicleRequestDto vehicleRequestDto) {


        // redis 에서 중복 조회
        String redisKey = "vehicle:"
                + vehicleRequestDto.getBrand() + ":"
                + vehicleRequestDto.getModel() + ":"
                + vehicleRequestDto.getYear();
        String statusRedisKey = "vehicle:status:" + vehicleRequestDto.getStatus();



        VehicleResponseDto cachedDto = (VehicleResponseDto) redisTemplate.opsForValue().get(redisKey);
        if (cachedDto != null) {
            throw new DuplicateVehicleException();
        }

        // 차량 중복 확인
        boolean exists = vehicleRepository.existsByBrandAndModelAndYear(
                vehicleRequestDto.getBrand(),
                vehicleRequestDto.getModel(),
                vehicleRequestDto.getYear()
        );
        if (exists) {
            throw new DuplicateVehicleException();
        }

        // Vehicle 엔티티 생성 및 저장
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(vehicleRequestDto.getBrand());
        vehicle.setModel(vehicleRequestDto.getModel());
        vehicle.setYear(vehicleRequestDto.getYear());
        vehicle.setStatus(vehicleRequestDto.getStatus());
        vehicle = vehicleRepository.save(vehicle);

        // Category 처리 및 VehicleCategory 저장
        Vehicle finalVehicle = vehicle;
        List<VehicleCategory> vehicleCategories = vehicleRequestDto.getCategories().stream()
                .map(categoryName -> {
                    // 카테고리 조회 또는 생성
                    Category category = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(categoryName);
                                return categoryRepository.save(newCategory);
                            });

                    // VehicleCategory 생성
                    VehicleCategory vehicleCategory = new VehicleCategory();
                    vehicleCategory.setVehicle(finalVehicle);
                    vehicleCategory.setCategory(category);
                    return vehicleCategoryRepository.save(vehicleCategory);
                })
                .collect(Collectors.toList());


        // VehicleResponseDto 생성 및 redis에 저장
        VehicleResponseDto vehicleResponseDto = new VehicleResponseDto();
        vehicleResponseDto.setId(vehicle.getId());
        vehicleResponseDto.setBrand(vehicle.getBrand());
        vehicleResponseDto.setModel(vehicle.getModel());
        vehicleResponseDto.setYear(vehicle.getYear());
        vehicleResponseDto.setStatus(vehicle.getStatus());
        vehicleResponseDto.setCategories(vehicleRequestDto.getCategories());


        // 차량정보 저장
        redisTemplate.opsForValue().set(redisKey, vehicleResponseDto, CACHE_TTL, TimeUnit.SECONDS);


        // 스테이터스 저장
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String vehicleJson = objectMapper.writeValueAsString(vehicleResponseDto);
            redisTemplate.opsForHash().put(statusRedisKey, vehicleResponseDto.getId().toString(), vehicleJson);
        } catch (JsonProcessingException e) {
            throw new SerializeException();
        }


        //카테고리 저장
        for(String category : vehicleRequestDto.getCategories()) {
            String categoryRediskey = "vehicle:category:" + category;

            try {
                // VehicleDto를 JSON으로 직렬화하여 Redis에 저장
                String vehicleJson = objectMapper.writeValueAsString(vehicleResponseDto);
                redisTemplate.opsForHash().put(categoryRediskey, vehicleResponseDto.getId().toString(), vehicleJson);
            } catch (JsonProcessingException e) {
                throw new SerializeException();
            }
        }





        return vehicleResponseDto;
    }

    // status 차량 조회
    public List<VehicleResponseDto> getVehiclesByStatus(String status) {

        String redisKey = "vehicle:status:" + status;

        Map<Object, Object> vehiclesMap = redisTemplate.opsForHash().entries(redisKey);
        ObjectMapper objectMapper = new ObjectMapper();
        if (!vehiclesMap.isEmpty()){
            return vehiclesMap.values().stream().map(json -> {
                try {
                    return objectMapper.readValue(json.toString(), VehicleResponseDto.class);
                } catch (Exception e) {
                    throw new DeserializeException();
                }
            }).collect(Collectors.toList());
        }

        // DB에서 데이터 조회
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithCategoriesByStatus(status);

        // 카테고리 조회 및 DTO 변환

        return vehicles.stream().map(vehicle -> {
            List<String> categories = vehicleCategoryRepository.findCategoryNamesByVehicleId(vehicle.getId());

            VehicleResponseDto dto = new VehicleResponseDto();
            dto.setId(vehicle.getId());
            dto.setBrand(vehicle.getBrand());
            dto.setModel(vehicle.getModel());
            dto.setYear(vehicle.getYear());
            dto.setStatus(vehicle.getStatus());
            dto.setCategories(categories);

            //Redis에 데이터 캐싱
            try {
                String vehicleJson = objectMapper.writeValueAsString(dto);
                redisTemplate.opsForHash().put(redisKey, dto.getId().toString(), vehicleJson);
            } catch (JsonProcessingException e) {
                throw new SerializeException();
            }
            return dto;
        }).collect(Collectors.toList());
    }


    // category 차량 조회
    public List<VehicleResponseDto> getVehiclesByCategory(String categoryName) {

        // redis 조회
        String redisKey = "category:" + categoryName;

        Map<Object, Object> vehiclesMap = redisTemplate.opsForHash().entries(redisKey);
        ObjectMapper objectMapper = new ObjectMapper();
        if (!vehiclesMap.isEmpty()){
            return vehiclesMap.values().stream().map(json -> {
                try {
                    return objectMapper.readValue(json.toString(), VehicleResponseDto.class);
                } catch (Exception e) {
                    throw new DeserializeException();
                }
            }).collect(Collectors.toList());
        }


        // Step 1: 카테고리로 차량 조회
        List<Vehicle> vehicles = vehicleCategoryRepository.findVehiclesByCategoryName(categoryName);

        // Step 2: Vehicle -> VehicleDto로 변환
        return vehicles.stream().map(vehicle -> {
            VehicleResponseDto dto = new VehicleResponseDto();
            dto.setId(vehicle.getId());
            dto.setBrand(vehicle.getBrand());
            dto.setModel(vehicle.getModel());
            dto.setYear(vehicle.getYear());
            dto.setStatus(vehicle.getStatus());
            dto.setCategories(vehicle.getCategories()
                    .stream()
                    .map(vc -> vc.getCategory().getName())
                    .collect(Collectors.toList()));


            // redis에 적재
            try {
                String vehicleJson = objectMapper.writeValueAsString(dto);
                redisTemplate.opsForHash().put(redisKey, dto.getId().toString(), vehicleJson);
            } catch (JsonProcessingException e) {
                throw new SerializeException();
            }

            return dto;
        }).collect(Collectors.toList());
    }




    @Transactional
    public VehicleResponseDto updateStatus(Long vehicleId, String newStatus) {
        // Step 1: DB에서 차량 조회
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Step 2: 기존 상태 저장
        String oldStatus = vehicle.getStatus();

        // Step 3: 상태 변경
        vehicle.setStatus(newStatus);
        vehicleRepository.save(vehicle);

        // dto 변환
        List<String> categories = vehicleCategoryRepository.findCategoryNamesByVehicleId(vehicle.getId());
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setStatus(vehicle.getStatus());
        dto.setCategories(categories);

        // Step 4: Redis 갱신
        updateRedisStatus(dto, oldStatus);

        return dto;
    }

    private void updateRedisStatus(VehicleResponseDto dto, String oldStatus) {
        String vehicleId = dto.getId().toString();

        // 기존 상태에서 제거
        String oldStatusKey = "vehicle:status:" + oldStatus;
        redisTemplate.opsForHash().delete(oldStatusKey, vehicleId);


        // 새로운 상태에 추가
        String newStatusKey = "vehicle:status:" + dto.getStatus();
        try {
            String vehicleJson = new ObjectMapper().writeValueAsString(dto);
            redisTemplate.opsForHash().put(newStatusKey, dto.getId().toString(), vehicleJson);
        } catch (JsonProcessingException e) {
            throw new SerializeException();
        }

        // 카테고리 갱신
        for (String category : dto.getCategories()) {
            String categoryKey = "category:" + category;
            try {
                String vehicleJson = new ObjectMapper().writeValueAsString(dto);
                redisTemplate.opsForHash().put(categoryKey, dto.getId().toString(), vehicleJson);
            } catch (JsonProcessingException e) {
                throw new SerializeException();
            }
        }


        // 차량 세부 정보 갱신
        String vehicleKey = "vehicle:"
                + dto.getBrand() + ":"
                + dto.getModel() + ":"
                + dto.getYear();
        redisTemplate.opsForValue().set(vehicleKey, dto, CACHE_TTL, TimeUnit.SECONDS);
    }



    @Transactional
    public VehicleResponseDto updateCategories(Long vehicleId, List<String> newCategories) {
        // Step 1: DB에서 차량 조회
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(NotFoundException::new);

        // Step 2: 기존 카테고리 조회
        Set<String> oldCategories = vehicle.getCategories().stream()
                .map(vc -> vc.getCategory().getName())
                .collect(Collectors.toSet());

        Set<VehicleCategory> currentCategories = vehicle.getCategories();

        currentCategories.clear();

        // Step 3: 카테고리 업데이트
        newCategories.forEach(categoryName -> {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(categoryName);
                        return categoryRepository.save(newCategory);
                    });

            VehicleCategory vehicleCategory = new VehicleCategory();
            vehicleCategory.setVehicle(vehicle);
            vehicleCategory.setCategory(category);
            currentCategories.add(vehicleCategory);
        });
        vehicleRepository.save(vehicle);

        // dto 변경 및 Redis 갱신

        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setStatus(vehicle.getStatus());
        List<String> tmp = new ArrayList<>(vehicle.getCategories().stream()
                .map(vc -> vc.getCategory().getName())
                .toList());

        dto.setCategories(tmp);

        updateRedisCategories(dto, oldCategories);

        return dto;
    }

    private void updateRedisCategories(VehicleResponseDto dto, Set<String> oldCategories) {
        String vehicleId = dto.getId().toString();


        // 기존 카테고리에서 제거
        for (String oldCategory : oldCategories) {
            String oldCategoryKey = "category:" + oldCategory;
            redisTemplate.opsForHash().delete(oldCategoryKey, vehicleId);
        }


        // 새로운 카테고리에 추가
        for (String newCategory : dto.getCategories()) {
            String newCategoryKey = "category:" + newCategory;
            try {
                String vehicleJson = new ObjectMapper().writeValueAsString(dto);
                redisTemplate.opsForHash().put(newCategoryKey, dto.getId().toString(), vehicleJson);
            } catch (JsonProcessingException e) {
                throw new SerializeException();
            }
        }


        // status 갱신
        String statusKey = "vehicle:status:" + dto.getStatus();
        try {
            String vehicleJson = new ObjectMapper().writeValueAsString(dto);
            redisTemplate.opsForHash().put(statusKey, dto.getId().toString(), vehicleJson);
        } catch (JsonProcessingException e) {
            throw new SerializeException();
        }

        // 차량 세부 정보 갱신
        String vehicleKey = "vehicle:"
                + dto.getBrand() + ":"
                + dto.getModel() + ":"
                + dto.getYear();
        redisTemplate.opsForValue().set(vehicleKey, dto, CACHE_TTL, TimeUnit.SECONDS);
    }

}