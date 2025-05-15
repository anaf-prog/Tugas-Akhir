package com.unsia.netinv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "backup_routes")
@Data
public class BackupRoutes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_device_id",referencedColumnName = "id", nullable = false)
    private Device mainDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_device_id", referencedColumnName = "id", nullable = false)
    private Device backupDevice;
}
