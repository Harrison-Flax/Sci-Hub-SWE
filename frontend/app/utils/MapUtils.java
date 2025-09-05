package utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Tai-Chia Huang
 */

public class MapUtils {

    public static Map<String, Object> toMap(boolean removeNullValues, Object... values) {
        return toMap(removeNullValues, String.class, Object.class, values);
    }

    public static Map<String, Object> toMap(Object... values) {
        return toMap(false, values);
    }

    public static <K, V> Map<K, V> toMap(boolean removeNullValues, Class<K> keyElement, Class<V> valueElement,
                                         Object[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyMap();
        }

        if (values.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "The array length is " + values.length + ", it must be even and maps to key/value pairs.");
        }

        Map<K, V> map = new LinkedHashMap<K, V>();
        for (int i = 0; i < values.length; i += 2) {
            K key = keyElement.cast(values[i]);
            V value = valueElement.cast(values[i + 1]);
            if (value != null || (value == null && !removeNullValues)) {
                map.put(key, value);
            }
        }

        return map;
    }
}
