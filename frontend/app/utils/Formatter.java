package utils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Formatter {

    /**
     * This utility method deserializes a json node to a java class object.
     * @param jsonNode
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T deserialize(JsonNode jsonNode, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T instance = mapper.readValue(jsonNode.toString(), valueType);
            return instance;
        } catch (Exception e) {
            Logger.error(valueType.getName() + " deserialize failed in Formatter..." + e.toString());
            return null;
        }
    }

    /**
     * This utility method deserialize json node to a list.
     * @param jsonNode
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> List<T> deserializeList(JsonNode jsonNode, Class<T> valueType) {
        if (jsonNode == null) return null;
        List<T> instanceList = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode json = jsonNode.path(i);
            T instance = deserialize(json, valueType);
            if (instance != null) {
                instanceList.add(instance);
            } else {
                Logger.error(valueType.getName() +
                        " list deserialize from json failed in Formatter.deserializeList() in item: " + i);
            }
        }
        return instanceList;
    }


    public static String splitCamelCase(String s) {
        String tmp = s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
        tmp = StringUtils.capitalize(tmp);
        return tmp;
    }

    public static String capitalize(String s) {
        s = s.replace('_', ' ');
        return WordUtils.capitalize(s);
    }
}