package com.parkease.parkinglotservice.repository;

import com.parkease.parkinglotservice.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByAdminApprovedTrueAndIsOpenTrue();
    List<ParkingLot> findByAdminApprovedTrue();
    List<ParkingLot> findByManagerId(Long managerId);
    @Query("SELECT p FROM ParkingLot p WHERE p.adminApproved = false")
    List<ParkingLot> findByAdminApprovedFalse();

    @Query("SELECT p FROM ParkingLot p WHERE p.adminApproved = true AND " +
           "(LOWER(p.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.landmark) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<ParkingLot> searchLots(@Param("query") String query);
}
