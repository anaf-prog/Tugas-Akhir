package com.unsia.netinv.service;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.dto.ReportEdit;
import com.unsia.netinv.dto.ReportForm;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Report;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.netinve.ReportStatus;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.repository.ReportRepository;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private FailOverLogRepository failOverLogRepository;

    @Override
    public void createReport(ReportForm reportForm) {
        Device device = deviceRepository.findById(reportForm.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device tidak ditemukan"));

        Report report = new Report();
        report.setDevice(device);
        report.setIssueDate(reportForm.getIssueDate());
        report.setIssueDescription(reportForm.getIssueDescription());
        report.setRepairDate(reportForm.getRepairDate());
        report.setRepairDescription(reportForm.getRepairDescription());
        report.setTechnician(reportForm.getTechnician());
        report.setStatus(reportForm.getStatus());

        reportRepository.save(report);

        System.out.println("Mencari failover log untuk device: " + device.getDeviceName());

        // CARI FailOverLogs yang belum diperbaiki DAN waktunya sebelum repairDate
        List<FailOverLogs> relatedFailovers = failOverLogRepository
            .findByMainDeviceAndRepairTimeIsNullAndWaktuBefore(
                device, 
                report.getRepairDate()
            );

        System.out.println("Jumlah failover ditemukan: " + relatedFailovers.size());

        // Ubah repairTime di log yang waktu down-nya sebelum repairDate
        for (FailOverLogs log : relatedFailovers) {
            log.setRepairTime(report.getRepairDate());
            failOverLogRepository.save(log); 
        }

        if (reportForm.getStatus() == ReportStatus.SELESAI) {
            device.setStatusDevice("ONLINE");
            deviceRepository.save(device);
        }
    }

    public static String formatDate(Date date) {
        if (date == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public Map<String, Object> getReportPageData(Users user) {
        Map<String, Object> data = new HashMap<>();

        List<Report> reports = reportRepository.findAllByOrderByIssueDateDesc();
        List<Device> devices = deviceRepository.findAll();

        List<MonitoringLog> allLogs = monitoringLogRepository.findAllByOrderByMonitoringDesc();

        // Proses untuk menentukan logReason yang benar
        processLogReasons(allLogs);
        data.put("formatDate", new Object() {
            @SuppressWarnings("unused")
            public String apply(Date date) {
                return formatDate(date);
            }
        });

        data.put("currentUser", user.getUsername());
        data.put("userRole", user.getRole());
        data.put("reports", reports);
        data.put("devices", devices);
        data.put("monitoringLogs", allLogs);

        return data;
    }

    /**
     * Method untuk memproses dan mengupdate logReason berdasarkan status ping dan log sebelumnya
     */
    private void processLogReasons(List<MonitoringLog> logs) {
        // Kelompokkan log berdasarkan device
        Map<Device, List<MonitoringLog>> logsByDevice = logs.stream()
            .collect(Collectors.groupingBy(MonitoringLog::getDevice));
        
        // Proses setiap device
        for (Map.Entry<Device, List<MonitoringLog>> entry : logsByDevice.entrySet()) {
            Device device = entry.getKey();
            List<MonitoringLog> deviceLogs = entry.getValue();
            
            // Urutkan secara ascending (dari yang terlama ke terbaru)
            deviceLogs.sort(Comparator.comparing(MonitoringLog::getMonitoring));
            
            // Proses setiap log untuk device ini
            for (int i = 0; i < deviceLogs.size(); i++) {
                MonitoringLog currentLog = deviceLogs.get(i);
                
                if (currentLog.getPingStatus()) { // Jika status UP
                    if (i > 0) {
                        MonitoringLog previousLog = deviceLogs.get(i - 1);
                        if (!previousLog.getPingStatus()) {
                            // Jika sebelumnya DOWN, sekarang UP → RECOVERED
                            currentLog.setLogReason(LogReason.RECOVERED);
                        } else {
                            // Jika sebelumnya UP, sekarang tetap UP → NORMAL
                            currentLog.setLogReason(LogReason.NORMAL);
                        }
                    } else {
                        // Jika tidak ada log sebelumnya, asumsikan NORMAL
                        currentLog.setLogReason(LogReason.NORMAL);
                    }
                } else { // Jika status DOWN
                    // Cek apakah device dalam status maintenance
                    if ("MAINTENANCE".equalsIgnoreCase(device.getStatusDevice())) {
                        currentLog.setLogReason(LogReason.MAINTENANCE);
                    } else {
                        currentLog.setLogReason(LogReason.DOWN);
                    }
                }
                
                // Simpan perubahan ke database
                monitoringLogRepository.save(currentLog);
            }
        }
    }

    @Override
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan"));
    }

    @Override
    public Map<String, Object> getEditReportPageData(Long id) {
        Report report = getReportById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("reportDTO", new ReportEdit());
        data.put("statusValues", ReportStatus.values());

        return data;
    }

    @Override
    public Map<String, Object> updateReport(Long id, ReportEdit dto) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan"));

        report.setIssueDate(dto.getIssueDate());
        report.setIssueDescription(dto.getIssueDescription());
        report.setRepairDate(dto.getRepairDate());
        report.setRepairDescription(dto.getRepairDescription());
        report.setTechnician(dto.getTechnician());
        report.setStatus(dto.getStatus());

        reportRepository.save(report);

        Map<String, Object> result = new HashMap<>();
        result.put("updatedReport", report);
        
        return result;
    }

    @Override
    public Map<String, Object> getEditPageDataOnValidationError(Long id) {
        Report report = reportRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan"));

        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("statusValues", ReportStatus.values());

        return data;
    }

    @Override
    public Map<String, Object> deleteReportById(Long id) {
        try {
            reportRepository.deleteById(id);
            return Map.of(
                "success", true,
                "message", "Data berhasil dihapus"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Data gagal dihapus: " + e.getMessage()
            );
        }
    }
    
}
