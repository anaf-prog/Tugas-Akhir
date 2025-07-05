package com.unsia.netinv.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.MaintenanceLogRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @GetMapping
    public String showPerangkatPage(@RequestParam(value = "success", required = false) String success,
                                  @RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "search", required = false) String searchTerm,
                                  Model model, 
                                  HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Handle notifications
        if (success != null) model.addAttribute("successMessage", success);
        if (error != null) model.addAttribute("errorMessage", error);

        // Get paginated data
        Page<MaintenanceLog> pageResult = getPaginatedData(page, size, searchTerm);

        // Add attributes to model
        addAttributesToModel(model, user, pageResult, page, size, searchTerm);
        
        return "maintenance";
    }

    private Page<MaintenanceLog> getPaginatedData(int page, int size, String searchTerm) {
        if (StringUtils.hasText(searchTerm)) {
            String processedSearchTerm = processSearchTerm(searchTerm);
            
            return maintenanceLogRepository.searchMaintenanceLogs(
                processedSearchTerm,
                PageRequest.of(page, size, Sort.by("maintenanceDate").descending())
            );
        }
        return maintenanceLogRepository.findAllWithDeviceOrderedByMaintenanceDateDesc(
            PageRequest.of(page, size, Sort.by("maintenanceDate").descending())
        );
    }

    private String processSearchTerm(String searchTerm) {
        try {
            // Decode URL encoding
            String decodedTerm = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8.toString());
            
            // Proses pencarian
            return "%" + decodedTerm.trim().toLowerCase().replace(" ", "%") + "%";
        } catch (UnsupportedEncodingException e) {
            // Fallback jika decoding gagal
            return "%" + searchTerm.trim().toLowerCase().replace(" ", "%") + "%";
        }
    }

    private void addAttributesToModel(Model model, Users user, Page<MaintenanceLog> pageResult, 
                                    int page, int size, String searchTerm) {

        // Proses setiap log untuk menambahkan status maintenanceOver
        List<MaintenanceLog> processedLogs = pageResult.getContent().stream()
            .peek(log -> {
                boolean isMaintenanceOver = log.getRepairCompletionTime() != null && 
                                         LocalDateTime.now().isAfter(log.getRepairCompletionTime());
                log.setMaintenanceOver(isMaintenanceOver);
            }).collect(Collectors.toList());                                

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("maintenanceLogs", processedLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("searchQuery", searchTerm != null ? searchTerm : "");
    }
    
}
