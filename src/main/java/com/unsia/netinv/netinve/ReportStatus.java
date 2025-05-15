package com.unsia.netinv.netinve;

public enum ReportStatus {
    SELESAI("Selesai"),
    DALAM_PERBAIKAN("Dalam Perbaikan"),
    DITUNDA("Ditunda");
    
    private final String displayName;
    
    ReportStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
