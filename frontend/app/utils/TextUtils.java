package utils;

import org.apache.commons.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

/**
 * @author Tai-Chia Huang
 */

public class TextUtils {

    /**
     * Perform string interpolation
     * @<code>
     *     String str = "/conversation/${userId}/length";
     *     String result = TextUtils.replace(str, "userId", 1)
     * </code>
     *
     * @param prefix
     * @param source
     * @param values
     * @param <V>
     * @return formatted string
     */
    @SuppressWarnings("unchecked")
    public static <V> String replaceByPrefix(String prefix, Object source, Object... values) {
        Map<String, Object> valueMap = null;
        if (values != null && values.length == 1) {
            valueMap = (Map<String, Object>) values[0];
        } else {
            valueMap = MapUtils.toMap(values);
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(valueMap);
        if(StringUtils.isNotBlank(prefix)) {
            strSubstitutor.setVariablePrefix(prefix);
        }

        return strSubstitutor.replace(source);
    }

    public static <V> String replace(Object source, Object... values) {
        return replaceByPrefix(
                null, source, values);
    }

    public static String truncateString(int maxChar, String str){
        if ((str != null) && (str.length() > maxChar)) {
            return str.substring(0, maxChar - 3) + "...";
        }
        return str;
    }
}
