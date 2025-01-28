package com.example.vehicle.repository;

import com.example.vehicle.entity.Category;
import com.example.vehicle.entity.Vehicle;
import com.example.vehicle.entity.VehicleCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    @Query("SELECT vc.vehicle FROM VehicleCategory vc WHERE vc.category = :category")
    List<Vehicle> findVehiclesByCategory(@Param("category") Category category);

    @Query("SELECT vc.category.name FROM VehicleCategory vc WHERE vc.vehicle.id = :vehicleId")
    List<String> findCategoryNamesByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT vc.vehicle FROM VehicleCategory vc WHERE vc.category.name = :categoryName")
    List<Vehicle> findVehiclesByCategoryName(@Param("categoryName") String categoryName);

    void deleteByVehicle(Vehicle vehicle);
}