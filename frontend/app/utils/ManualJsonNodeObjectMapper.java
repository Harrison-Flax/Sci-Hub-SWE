package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;

/**
 * Created by zekunf on 11/6/2018.
 */
public class ManualJsonNodeObjectMapper {

    //zekunf Nov 6: Method moved from DS-FE-UserController
    public static User deserializeJsonToUser(JsonNode json) {
        User oneUser = new User();
        oneUser.setId(json.findPath("id").asLong());
        oneUser.setUserName(json.findPath("userName").asText());
        oneUser.setPassword(json.findPath("password").asText());
        oneUser.setFirstName(json.findPath("firstName").asText());
        oneUser.setMiddleInitial(json.findPath("middleInitial").asText());
        oneUser.setLastName(json.findPath("lastName").asText());
//        oneUser.setAffiliation(json.findPath("affiliation").asText());
        oneUser.setEmail(json.findPath("email").asText());
//        oneUser.setResearchFields(json.findPath("researchFields").asText());
        return oneUser;
    }
}
