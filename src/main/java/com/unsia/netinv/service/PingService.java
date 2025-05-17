package com.unsia.netinv.service;

public interface PingService {
    boolean pingDevice(String ipAddress);

    Long pingWithResponseTime(String ipAddress);

    void forceFailure(String ipAddress, boolean shouldFail);
}
