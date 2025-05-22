package com.unsia.netinv.controller;

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
import com.unsia.netinv.entity.Report;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.service.ReportService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public String createReport(@RequestBody @Valid ReportForm reportForm,
                               RedirectAttributes redirectAttributes,
                               BindingResult result) {
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat laporan: Data tidak valid");
            return "redirect:/reports";
        }

        try {
            reportService.createReport(reportForm);
            redirectAttributes.addFlashAttribute("successMessage", "Laporan berhasil dibuat!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat laporan: " + e.getMessage());
        }

        return "redirect:/reports";
    }

    @GetMapping
    public String listReports(Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Map<String, Object> reportData = reportService.getReportPageData(user);
        model.addAllAttributes(reportData);

        return "reports";
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        Report report = reportService.getReportById(id);
        model.addAttribute("report", report);

        return "report-detail";    
    }

    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        Map<String, Object> data = reportService.getEditReportPageData(id);
        model.addAllAttributes(data);

        return "report-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateReport(@PathVariable Long id, 
                               @Valid @ModelAttribute("reportDTO") ReportEdit reportDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes, 
                               Model model) {
        if (result.hasErrors()) {
            Map<String, Object> errorData = reportService.getEditPageDataOnValidationError(id);
            model.addAllAttributes(errorData);

            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui laporan: Data tidak valid");
            
            return "report-edit";
        }

        try {
            reportService.updateReport(id, reportDTO);
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
        Map<String, Object> result = reportService.deleteReportById(id);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
