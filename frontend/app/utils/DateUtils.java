package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Tai-Chia Huang
 */

public class DateUtils {
    public static final String COMPACT_FORMAT = "EEE MMM d HH:mm:ss z yyyy";

    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat(COMPACT_FORMAT);
        Date currDate = new Date();
        return df.format(currDate);
    }
}
