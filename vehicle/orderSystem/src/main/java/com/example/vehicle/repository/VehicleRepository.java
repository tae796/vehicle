package com.example.vehicle.repository;

import com.example.vehicle.entity.Vehicle;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Vehicle findByModel(String model);

    @Query("SELECT v FROM Vehicle v WHERE v.brand = :brand AND v.model = :model AND v.year = :year")
    Optional<Vehicle> findByBrandModelYear(@Param("brand") String brand,
                                           @Param("model") String model,
                                           @Param("year") Integer year);

    boolean existsByBrandAndModelAndYear(String brand, String model, Integer year);

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.categories vc LEFT JOIN FETCH vc.category WHERE v.status = :status")
    List<Vehicle> findVehiclesWithCategoriesByStatus(@Param("status") String status);

    Optional<Vehicle> findById(Long id);
}