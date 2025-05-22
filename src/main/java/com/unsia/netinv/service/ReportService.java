package com.unsia.netinv.service;

import java.util.Map;

import com.unsia.netinv.dto.ReportEdit;
import com.unsia.netinv.dto.ReportForm;
import com.unsia.netinv.entity.Report;
import com.unsia.netinv.entity.Users;

public interface ReportService {
    void createReport(ReportForm reportForm);
    Map<String, Object> getReportPageData(Users user);
    Report getReportById(Long id);
    Map<String, Object> getEditReportPageData(Long id);
    Map<String, Object> updateReport(Long id, ReportEdit dto);
    Map<String, Object> getEditPageDataOnValidationError(Long id);
    Map<String, Object> deleteReportById(Long id);
}
