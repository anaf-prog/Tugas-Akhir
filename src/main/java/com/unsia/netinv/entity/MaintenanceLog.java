package com.unsia.netinv.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Table(name = "maintenance_logs")
@Data
public class MaintenanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maintenance_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime maintenanceDate;

    @Column(name = "scheduled_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime scheduledTime;

    @Column(name = "repair_completion_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime repairCompletionTime;

    @Column(name = "auto_disable")
    private Boolean autoDisable = false;

    @Column(name = "descriptions")
    private String description;

    @Column(name = "technician")
    private String technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Device device;
}
