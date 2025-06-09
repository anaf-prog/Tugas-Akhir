package com.unsia.netinv.service;

import java.util.List;
import java.util.Map;

import com.unsia.netinv.entity.Users;

public interface DashboardService {
    Map<String, Object> getDashboardData(int devicePage, int deviceSize, int logPage, int logSize,
                                         int failoverPage, int failoverSize,
                                         String search, String status, String type, Users user);
    List<Map<String, Object>> getDownDevices();
}
