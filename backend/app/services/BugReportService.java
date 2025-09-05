package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.BugReport;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import utils.Common;

import java.util.List;
import java.util.Optional;

public class BugReportService {

    /**
     * This method changes a bug report object from jsonNode
     * @param bugReport
     * @param jsonNode
     * @throws Exception
     */
    public void updateBugReport(BugReport bugReport, JsonNode jsonNode) throws Exception {
        bugReport.setTitle(jsonNode.findPath("title").asText());
        bugReport.setDescription(jsonNode.findPath("description").asText());
        bugReport.setLongDescription(jsonNode.findPath("longDescription").asText());
    }

    /**
     * Sort the given list of bugReports
     *
     * @param bugReportList  List of bugReports to be sorted
     * @param sortOrder sort criteria
     * @return sorted list of bugReports.
     */
    public void sortBugReportList(List<BugReport> bugReportList, String sortOrder) {
        bugReportList.sort((bugReport1, bugReport2) -> {
            if (sortOrder.equals("date_created"))
                return bugReport2.getCreateTime().compareTo(bugReport1.getCreateTime());
            else if (sortOrder.equals("reporter_name"))
                return bugReport1.getTitle().toLowerCase().compareTo(bugReport2.getTitle().toLowerCase());
            else if (sortOrder.equals("title"))
                return bugReport1.getTitle().toLowerCase().compareTo(bugReport2.getTitle().toLowerCase());
            else
                return bugReport2.getCreateTime().compareTo(bugReport1.getCreateTime());
        });
    }

    /**
     * Gets a list of bugReports based on optional offset and pageLimit and sort criteria
     *
     * @param bugReportList     all bugReports
     * @param offset       shows the start index of the bugReport rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of bugReports.
     */
    public RESTResponse paginateResults(List<BugReport> bugReportList, Optional<Integer> offset, Optional<Integer> pageLimit, String sortCriteria) throws Exception {
        try {
            RESTResponse response = new RESTResponse();

            int maxRows = bugReportList.size();
            if (pageLimit.isPresent()) {
                maxRows = pageLimit.get();
            }

            //*************************paginate the list ************************************************************
            int startIndex = 0;
            if (offset.isPresent()) {
                startIndex = offset.get();
            }
            if (startIndex >= bugReportList.size())
                startIndex = pageLimit.get() * ((bugReportList.size() - 1) / pageLimit.get());
            // Set the sortCriteria order.
            response.setSort(sortCriteria.split(" ")[0]);
            response.setOffset(startIndex);

            List<BugReport> paginatedBugReports = Common.paginate(startIndex, maxRows, bugReportList);
            response.setTotal(bugReportList.size());
            ArrayNode bugReportJsonArray = Common.objectList2JsonArray(paginatedBugReports);
            response.setItems(bugReportJsonArray);
            return response;
        } catch (Exception e) {
            Logger.debug("BugReportService.paginateResults() exception: " + e.toString());
            throw e;
        }
    }

}
