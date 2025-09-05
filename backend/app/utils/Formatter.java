package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xnchen on 10/14/16.
 */
public class Formatter {

    public static <T> T deserialize(JsonNode jsonNode, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T instance = mapper.readValue(jsonNode.toString(), valueType);
            return instance;
        } catch (IOException e) {
        	e.printStackTrace();
            Logger.error(valueType.getName() + " deserialize failed...");
            return null;
        }
    }

    public static <T> List<T> deserializeList(JsonNode jsonNode, Class<T> valueType) {
        if (jsonNode == null) return null;
        List<T> instanceList = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode json = jsonNode.path(i);
            T instance = deserialize(json, valueType);
            if (instance != null) {
                instanceList.add(instance);
            } else {
                Logger.error(valueType.getName() + " list deserialize failed...");
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

    /**
     * Convert a JSON string to pretty print version
     *
     * @param jsonString
     * @return
     */
    public String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
}