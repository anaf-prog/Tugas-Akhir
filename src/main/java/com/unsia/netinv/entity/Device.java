package com.unsia.netinv.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "devices")
@Data
@ToString(exclude = {"location", "maintenanceLogs", "monitoringLogs", "mainBackupRoutes", "backupRoutes", "mainFailoverLogs", "backuoFailoverLogs"})
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nama perangkat wajib diisi")
    @Size(max = 255, message = "Nama perangkat maksimal 255 karakter")
    @Column (name = "device_name")
    private String deviceName = "Unknown Device";

    @NotBlank(message = "IP Address wajib diisi")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", 
             message = "Format IP Address tidak valid")
    @Column (name = "ip_address")
    private String ipAddress;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$|^$", 
             message = "Format MAC Address tidak valid (contoh: 00:1A:2B:3C:4D:5E)")
    @Column (name = "mac_address")
    private String macAddress;

    @NotBlank(message = "Jenis perangkat wajib dipilih")
    @Column (name = "device_type")
    private String deviceType = "OTHER";

    @NotBlank(message = "Status perangkat wajib dipilih")
    @Column (name = "status_device")
    private String statusDevice;

    @Column (name = "ping_status")
    private String pingStatus;

    @Column (name = "last_checked")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastChecked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceLog> maintenanceLogs = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MonitoringLog> monitoringLogs = new ArrayList<>();

    @OneToMany(mappedBy = "mainDevice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BackupRoutes> mainBackupRoutes = new ArrayList<>();

    @OneToMany(mappedBy = "backupDevice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BackupRoutes> backupRoutes = new ArrayList<>();

    @OneToMany(mappedBy = "mainDevice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FailOverLogs> mainFailoverLogs = new ArrayList<>();

    @OneToMany(mappedBy = "backupDevice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FailOverLogs> backuoFailoverLogs = new ArrayList<>();

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("deviceName", deviceName);
            map.put("ipAddress", ipAddress);
            map.put("macAddress", macAddress);
            map.put("deviceType", deviceType);
            map.put("statusDevice", statusDevice);
            map.put("lastChecked", lastChecked != null ? lastChecked.toString() : "N/A");

            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing di entity device : " + e.getMessage());
            return "{}";
        }
    }
}
