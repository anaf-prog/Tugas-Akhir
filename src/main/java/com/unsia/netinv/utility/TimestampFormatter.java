package com.unsia.netinv.utility;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimestampFormatter {
    public static String format(Timestamp timestamp, String pattern) {
        if (timestamp == null) return "";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(timestamp);
    }
}
