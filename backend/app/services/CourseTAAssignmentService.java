package services;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.CourseTAAssignment;
import models.rest.RESTResponse;
import utils.Common;

import java.util.List;
import java.util.Optional;

public class CourseTAAssignmentService {

    public RESTResponse paginateResults(List<CourseTAAssignment> assignments, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = assignments.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= assignments.size())
            startIndex = pageLimit.get() * ((assignments.size() - 1) / pageLimit.get());
        List<CourseTAAssignment> paginatedJobs = Common.paginate(startIndex, maxRows, assignments);
        response.setTotal(assignments.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json artay
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }

}
