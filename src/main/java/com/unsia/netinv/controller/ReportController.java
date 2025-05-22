package com.unsia.netinv.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unsia.netinv.dto.ReportEdit;
import com.unsia.netinv.dto.ReportForm;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Report;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.netinve.ReportStatus;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.repository.ReportRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reports")
public class ReportController {
    
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @PostMapping
    public String createReport(@RequestBody @Valid ReportForm reportForm,
                               RedirectAttributes redirectAttributes,
                               BindingResult result) {
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat laporan: Data tidak valid");
            return "redirect:/reports";
        }
        
         try {
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
            
            redirectAttributes.addFlashAttribute("successMessage", "Laporan berhasil dibuat!");
            return "redirect:/reports";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat laporan: " + e.getMessage());
            return "redirect:/reports";
        }
    }

    public static String formatDate(Date date) {
        if (date == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }

    @GetMapping
    public String listReports(Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Report> reports = reportRepository.findAll();
        List<Device> devices = deviceRepository.findAll();
        List<MonitoringLog> monitoringLogs = monitoringLogRepository.findAllByOrderByMonitoringDesc();

        model.addAttribute("formatDate", new Object() {
            @SuppressWarnings("unused")
            public String apply(Date date) {
                return formatDate(date);
            }
        });

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("reports", reports);
        model.addAttribute("devices", devices);
        model.addAttribute("monitoringLogs", monitoringLogs);

        return "reports";
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("laporan tidak ditemukan"));

        model.addAttribute("report", report);
        return "report-detail";    
    }

    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan"));
        
        model.addAttribute("report", report);
        model.addAttribute("reportDTO", new ReportEdit());
        model.addAttribute("statusValues", ReportStatus.values());

        return "report-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateReport(@PathVariable Long id, 
                               @Valid @ModelAttribute("reportDTO") ReportEdit reportDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes, 
                               Model model) {
        if (result.hasErrors()) {
            Report report = reportRepository.findById(id).orElseThrow();

            model.addAttribute("report", report);
            model.addAttribute("statusValues", ReportStatus.values());

            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui laporan: Data tidak valid");
            return "report-edit";
        }

        try {
            Report existingReport = reportRepository.findById(id).orElseThrow();
            existingReport.setIssueDate(reportDTO.getIssueDate());
            existingReport.setIssueDescription(reportDTO.getIssueDescription());
            existingReport.setRepairDate(reportDTO.getRepairDate());
            existingReport.setRepairDescription(reportDTO.getRepairDescription());
            existingReport.setTechnician(reportDTO.getTechnician());
            existingReport.setStatus(reportDTO.getStatus());

            reportRepository.save(existingReport);

            redirectAttributes.addFlashAttribute("successMessage", "Laporan berhasil diperbarui!");

            return "redirect:/reports/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui laporan: " + e.getMessage());

            return "redirect:/reports/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            reportRepository.deleteById(id);
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Data berhasil dihapus"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Data gagal dihapus: " + e.getMessage()
                ));
        }
    }
}
