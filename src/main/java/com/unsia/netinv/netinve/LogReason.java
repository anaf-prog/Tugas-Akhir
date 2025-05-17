package com.unsia.netinv.netinve;

public enum LogReason {
    DOWN("Down"),
    HIGH_LATENCY("High Latency"),
    NORMAL("Normal"),
    RECOVERED("Recovered");

    private final String displayName;

    LogReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
