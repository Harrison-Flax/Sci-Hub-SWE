package services;

import play.mvc.*;
import java.io.File;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import javax.inject.Inject;
import play.mvc.Result;
import play.mvc.Results;
import play.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import utils.RESTfulCalls;
import utils.Constants;
import play.libs.Json;

public class FileService {

    private final Config config;

    @Inject
    public FileService(Config config) {
        this.config = config;
    }
    public static String getUploadedFilePath(Http.MultipartFormData body, String fieldName, ObjectNode jsonData) {
        Http.MultipartFormData.FilePart<File> filePart = body.getFile(fieldName);
        if (filePart != null) {
            File file = filePart.getFile();
            if (file != null && file.length() > 0) {
                String filePath = file.getAbsolutePath();
                jsonData.put(fieldName, filePath);
            } else {
            }
        }
        return null;
    }

    public Boolean checkFile(String tableName, String fileType, String tableRecorderId) {
        try {
            String apiPath = Constants.CHECK_FILE + tableName + "/" + fileType + "/" + tableRecorderId;
            String fullUrl = RESTfulCalls.getBackendAPIUrl(config, apiPath);

            JsonNode response = RESTfulCalls.getAPI(fullUrl);
            Logger.info("Received response from backend API: " + response.toString());

            if (response.has("dbRecord") && !response.get("dbRecord").isNull()) {
                return true;
            } else {
                Logger.warn("Response does not contain 'dbRecord'.");
            }
        } catch (Exception e) {
            Logger.error("Exception occurred in checkFile: " + e.getMessage(), e);
        }
        Logger.info("Returning false. File check failed for tableName: " + tableName +
                ", fileType: " + fileType + ", tableRecorderId: " + tableRecorderId);
        return false;
    }

    public JsonNode uploadFile(Http.MultipartFormData body,
                               String fieldName,
                               String tableName,
                               String fileType,
                               Long recordId) {
        Http.MultipartFormData.FilePart<File> part = body.getFile(fieldName);

        if (part == null || part.getFile() == null || part.getFile().length() == 0) {
            Logger.warn("uploadFile: No file uploaded or file is empty for field: " + fieldName);
            return null;
        }

        File file = part.getFile();

        String endpoint = Constants.FILE_UPLOAD_ENDPOINT
                + "/" + tableName
                + "/" + fileType
                + "/" + recordId;
        String url = RESTfulCalls.getBackendAPIUrl(config, endpoint);

        Logger.debug("uploadFile: Uploading file to URL: " + url);
        return RESTfulCalls.postAPIWithFile(url, file);
    }
}

