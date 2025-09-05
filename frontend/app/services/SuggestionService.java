package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Suggestion;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;

import javax.inject.Inject;

public class SuggestionService {
    @Inject
    Config config;


    /**
     * This method prepares a json node from Form for suggestion registration.
     * @param suggestionForm
     * @param userId
     * @return
     */
    public static ObjectNode prepareJsonNodeFromFormForRegistration(Form<Suggestion> suggestionForm, String userId) {
        ObjectNode jsonData = Json.newObject();
        try {
            jsonData.put("title", suggestionForm.field("title").value());
            jsonData.put("userId", userId);
            jsonData.put("description", suggestionForm.field("description").value());
            jsonData.put("longDescription", suggestionForm.field("longDescription").value());
            jsonData.put("solved", 0);
        } catch (Exception e) {
            Logger.debug("SuggestionService.prepareJsonNodeFromFormForRegistration() exception: " + e.toString());
            throw e;
        }
        return jsonData;
    }

    /**
     * This method prepares a json node from Form for suggestion edit.
     * @param suggestionForm
     * @return
     */
    public static ObjectNode prepareJsonNodeFromFormForEdit(Form<Suggestion> suggestionForm) {
        ObjectNode jsonData = Json.newObject();
        try {
            jsonData.put("id", suggestionForm.field("id").value());
            jsonData.put("title", suggestionForm.field("title").value());
            jsonData.put("email", suggestionForm.field("email").value());
            jsonData.put("name", suggestionForm.field("name").value());
            jsonData.put("organization", suggestionForm.field("organization").value());
            jsonData.put("description", suggestionForm.field("description").value());
            jsonData.put("longDescription", suggestionForm.field("longDescription").value());
            jsonData.put("solved", suggestionForm.field("solved").value());
            jsonData.put("createdTime", suggestionForm.field("createdTime").value());
            jsonData.put("solvedTime", suggestionForm.field("solvedTime").value());
        } catch (Exception e) {
            Logger.debug("SuggestionService.prepareJsonNodeFromFormForRegister() exception: " + e.toString());
            throw e;
        }
        return jsonData;
    }


}
