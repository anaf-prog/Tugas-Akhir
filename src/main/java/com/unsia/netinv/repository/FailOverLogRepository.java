package com.unsia.netinv.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.FailOverLogs;

@Repository
public interface FailOverLogRepository extends JpaRepository<FailOverLogs, Long>, JpaSpecificationExecutor<FailOverLogs> {

    Page<FailOverLogs> findAllByOrderByWaktuDesc(Pageable pageable);
}
