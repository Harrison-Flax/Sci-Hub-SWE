package utils;

/**
 * @author Tai-Chia Huang
 */

public class NumberUtils {

    public static boolean isNumber(String str) {
        try {
            double v = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return false;
    }
}
