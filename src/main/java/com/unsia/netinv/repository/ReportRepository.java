package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    
    List<Report> findAllByOrderByIssueDateDesc();

    List<Report> findLatestByDeviceId(Long id);

    Optional<Report> findTopByDeviceOrderByRepairDateDesc(Device device);
}
