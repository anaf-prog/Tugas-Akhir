package com.unsia.netinv.entity;

import java.util.Date;

import com.unsia.netinv.netinve.LogReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "monitoring_logs")
@Data
public class MonitoringLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ping_status")
    private Boolean pingStatus;

    @Column(name = "response_time")
    private Integer responseTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "monitoring")
    private Date monitoring;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_reason", length = 50)
    private LogReason logReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;
}
