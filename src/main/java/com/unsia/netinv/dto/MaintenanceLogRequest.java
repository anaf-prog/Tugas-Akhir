package com.unsia.netinv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceLogRequest {
    private DeviceDTO device;
    private String MaintenanceDate;
    private String technician;
    private String description;

    @Data
    public static class DeviceDTO {
        private Long id;
    }
}
