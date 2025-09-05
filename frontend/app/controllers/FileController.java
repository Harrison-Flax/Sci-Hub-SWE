package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.Logger;
import play.mvc.*;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.generalError;

import javax.inject.Inject;

import static controllers.Application.checkLoginStatus;

public class FileController extends Controller {
    @Inject
    Config config;
    public Result getFile(String tableName, String fileType, String tableRecorderId) {
        String apiPath = Constants.FILE + tableName + "/" + fileType + "/" + tableRecorderId;
        String fullUrl = RESTfulCalls.getBackendAPIUrl(config, apiPath);
        Logger.info("Requesting backend API: " + fullUrl);

        try {
            JsonNode response = RESTfulCalls.getAPI(fullUrl);

            if (response.has("error")) {
                Logger.debug("FileService.getFileById() did not get file from backend with error.");
                return null;
            }

            if (response.has("fileContent")) {
                String base64Content = response.get("fileContent").asText();
                byte[] fileContent = java.util.Base64.getDecoder().decode(base64Content);

                if (response.has("fileDetails")) {
                    JsonNode fileDetails = response.get("fileDetails");
                }

                return ok(fileContent).as("application/pdf");
            }

            return notFound("File content not found");
        } catch (Exception e) {
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
}
