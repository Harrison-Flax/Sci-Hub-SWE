package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.File;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.S3Utils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import play.mvc.Http.RawBuffer;


@Singleton
public class FileController extends Controller {
    private static final String BUCKET_NAME = Constants.AWS_BUCKET_NAME;
    private static final String FILE_NAME_PREFIX = Constants.AWS_FILE_NAME_PREFIX;

    private Result createErrorResponse(int statusCode, String message) {
        ObjectNode errorJson = Json.newObject();
        errorJson.put("status", statusCode);
        errorJson.put("message", message);
        return status(statusCode, errorJson);
    }

    private boolean isErrorResult(Result result) {
        int statusCode = result.status();
        return statusCode >= 400 && statusCode < 600;
    }

    public Result getFile(String tableName, String fileType, String tableRecorderId) {
        try {
            if (tableName == null || tableName.isEmpty()) {
                return createErrorResponse(400, "tableName cannot be empty");
            }

            if (fileType == null || fileType.isEmpty()) {
                return createErrorResponse(400, "fileType cannot be empty");
            }

            if (tableRecorderId == null || tableRecorderId.isEmpty()) {
                return createErrorResponse(400, "tableRecorderId cannot be empty");
            }

            File dbRecord = fetchFileFromDatabase(tableName, fileType, tableRecorderId);
            if (dbRecord == null) {
                Logger.warn("No record found in DB for tableName=" + tableName + ", fileType=" + fileType + ", tableRecorderId=" + tableRecorderId);
                return createErrorResponse(404, "No file found for the given parameters.");
            }

            String filePath = dbRecord.getPath();
            if (filePath == null || filePath.isEmpty()) {
                Logger.warn("DB record found but path is empty, tableName=" + tableName + ", fileType=" + fileType + ", tableRecorderId=" + tableRecorderId);
                return createErrorResponse(404, "File path not set in DB for the given parameters.");
            }

            String base64Content = fetchFileFromS3(filePath);
            if (base64Content == null || base64Content.isEmpty()) {
                Logger.warn("S3 object content is empty for filePath: " + filePath);
                return createErrorResponse(404, "File not found in S3.");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileContent", base64Content);

            return ok(Json.toJson(result));
        } catch (Exception e) {
            Logger.error("Error retrieving file: " + e.getMessage(), e);
            return createErrorResponse(500, "An unexpected error occurred.");
        }
    }

    private File fetchFileFromDatabase(String tableName, String fileType, String tableRecorderId) {
        return File.find.query()
                .where()
                .eq("tableName", tableName)
                .eq("fileType", fileType)
                .eq("tableRecorderId", tableRecorderId)
                .findOne();
    }
    public Result checkFile(String tableName, String fileType, String tableRecorderId) {
        File dbRecord = File.find.query()
                        .where()
                        .eq("tableName", tableName)
                        .eq("fileType", fileType)
                        .eq("tableRecorderId", tableRecorderId)
                        .findOne();
        Map<String, Object> result = new HashMap<>();
        result.put("dbRecord", dbRecord);

        if (dbRecord != null) {
            Logger.info("CheckFile: Record found for tableName={}, fileType={}, tableRecorderId={}. Record details: {}",
                    tableName, fileType, tableRecorderId, dbRecord);
        } else {
            Logger.warn("CheckFile: No record found for tableName={}, fileType={}, tableRecorderId={}",
                    tableName, fileType, tableRecorderId);
        }

        return ok(Json.toJson(result));
    }

    private String fetchFileFromS3(String filePath) {
        String s3Key = extractS3Key(filePath);
        return S3Utils.getObject(BUCKET_NAME, s3Key);
    }

    private String extractS3Key(String s3Url) {
        String prefix = "https://" + BUCKET_NAME + ".s3.amazonaws.com/";
        if (s3Url.startsWith(prefix)) {
            return s3Url.substring(prefix.length());
        }
        throw new IllegalArgumentException("Invalid S3 URL: " + s3Url);
    }

    public Result uploadRawFile(String tableName, String fileType, Long recordId) {
        RawBuffer buf = request().body().asRaw();
        java.io.File localFile = buf.asFile();
        if (localFile == null || !localFile.exists() || localFile.length() == 0) {
            ObjectNode err = Json.newObject().put("error", "no file uploaded");
            return badRequest(err);
        }

        String ext    = fileType.toLowerCase().contains("pdf") ? ".pdf" : "";
        String name   = fileType + "_" + tableName + "_" + recordId + ext;
        String s3Key  = FILE_NAME_PREFIX + tableName + "/" + name;

        String mime   = ext.equals(".pdf") ? "application/pdf" : "application/octet-stream";
        String s3Url  = S3Utils.uploadFile(BUCKET_NAME, localFile, s3Key, mime, "", "");
        if (s3Url == null) {
            ObjectNode err = Json.newObject().put("error", "upload failed");
            return internalServerError(err);
        }
        File record = File.find.query()
                .where()
                .eq("tableName", tableName)
                .eq("fileType", fileType)
                .eq("tableRecorderId", recordId.toString())
                .findOne();
        if (record == null) record = new File();
        record.setTableName(tableName);
        record.setFileType(fileType);
        record.setTableRecorderId(recordId.toString());
        record.setFileName(name);
        record.setPath(s3Url);
        record.save();

        localFile.delete();

        return ok(Json.toJson(record));
    }
}
