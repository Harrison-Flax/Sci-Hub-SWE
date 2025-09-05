package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.libs.Json;
import utils.Constants;
import utils.RESTfulCalls;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AccessTimesService {

    @Inject
    Config config;
    public void AddOneTime(String databaseName, Long id){
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(id));
            map.put("databaseName", databaseName);
            JsonNode json = Json.toJson(map);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(
                    config, Constants.ACCESS_TIME_PLUS_ONE), json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
