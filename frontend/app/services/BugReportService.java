package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.BugReport;
import play.Logger;
import play.data.Form;
import play.libs.Json;

import javax.inject.Inject;

public class BugReportService {
    @Inject
    Config config;

    /**
     * This method prepares a json node from Form for bug report registerPOST.
     *
     * @param bugReportForm
     * @return
     */
//    public static ObjectNode prepareJsonNodeFromFormForRegister(Form<BugReport> bugReportForm, String userId) {
//        ObjectNode jsonData = Json.newObject();
//        try {
//            jsonData.put("title", bugReportForm.field("title").value());
//            jsonData.put("userId", userId);
//            jsonData.put("description", bugReportForm.field("description").value());
//            jsonData.put("longDescription", bugReportForm.field("longDescription").value());
//            jsonData.put("solved", 0);
//        } catch (Exception e) {
//            Logger.debug("BugReportService.prepareJsonNodeFromFormForRegister() exception: " + e.toString());
//            throw e;
//        }
//        return jsonData;
//    }

    /**
     * This method prepares a json node from Form for bug report editPOST.
     *
     * @param bugReportForm
     * @return
     */
//    public static ObjectNode prepareJsonNodeFromFormForEdit(Form<BugReport> bugReportForm) {
//        ObjectNode jsonData = Json.newObject();
//        try {
//            jsonData.put("id", bugReportForm.field("id").value());
//            jsonData.put("title", bugReportForm.field("title").value());
//            jsonData.put("email", bugReportForm.field("email").value());
//            jsonData.put("name", bugReportForm.field("name").value());
//            jsonData.put("organization", bugReportForm.field("organization").value());
//            jsonData.put("description", bugReportForm.field("description").value());
//            jsonData.put("longDescription", bugReportForm.field("longDescription").value());
//            jsonData.put("solved", bugReportForm.field("solved").value());
//            jsonData.put("createdTime", bugReportForm.field("createdTime").value());
//            jsonData.put("solvedTime", bugReportForm.field("solvedTime").value());
//        } catch (Exception e) {
//            Logger.debug("BugReportService.prepareJsonNodeFromFormForEdit() exception: " + e.toString());
//            throw e;
//        }
//        return jsonData;
//    }
}
