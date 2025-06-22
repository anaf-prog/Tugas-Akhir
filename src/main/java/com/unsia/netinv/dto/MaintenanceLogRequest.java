package com.unsia.netinv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceLogRequest {
    @NotNull(message = "Device ID tidak boleh kosong")
    private Long deviceId;
    
    @NotBlank(message = "Tanggal maintenance tidak boleh kosong")
    private String maintenanceDate;
    
    private String scheduledTime;
    private Boolean autoDisable = false;
    
    @NotBlank(message = "Nama teknisi tidak boleh kosong")
    private String technician;
    
    @NotBlank(message = "Deskripsi tidak boleh kosong")
    private String description;

    private String repairCompletionTime;

}
