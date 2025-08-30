package com.unsia.netinv.entity;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.unsia.netinv.netinve.RecoveryType;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "failover_logs")
@Data
public class FailOverLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_time")
    private LocalDateTime repairTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "waktu")
    private Date waktu;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "status")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "recovery_type")
    private RecoveryType recoveryType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_device_id",referencedColumnName = "id", nullable = false)
    private Device mainDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_device_id", referencedColumnName = "id", nullable = false)
    private Device backupDevice;

    @Transient
    public Date getRepairDate() {
        // Diisi oleh service
        return null;
    }

    @Column(name = "estimated_repair_time")
    private LocalDateTime estimatedRepairTime;

    @PrePersist
    public void setDefaultEstimation() {
        if (this.estimatedRepairTime == null) {
            this.estimatedRepairTime = this.waktu.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusMinutes(60); // Default +1 jam
        }
    }

    @Transient
    public LocalDateTime getDisplayRepairTime() {
        return repairTime != null ? repairTime : estimatedRepairTime;
    }

    @Transient
    public String getFormattedRepairDate() {
        Date repairDate = getRepairDate();
        return repairDate != null ? 
               new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(repairDate) : 
               "-";
    }

    /** Waktu setelah perangkat benar-benar pindah jalur (waktu + responseTimeMs) */
    @Transient
    public Date getWaktuSetelahPindah() {
        if (waktu == null || responseTimeMs == null) {
            return waktu;               // biar nggak NPE kalau ada data null
        }
        return new Date(waktu.getTime() + responseTimeMs);   // tambah milidetik
    }
    
}
