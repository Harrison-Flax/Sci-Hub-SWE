package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.TAJob;
import models.rest.RESTResponse;
import utils.Common;

import java.util.List;
import java.util.Optional;


public class TAJobService {
    /**
     * This method intends to return a list of TA jobs based on optional offset and pageLimit and sort criteria
     *
     * @patam tajobs   all TA jobs
     * @patam offset       shows the start index of the jobs rows we want to receive
     * @patam pageLimit    shows the number of rows we want to receive
     * @patam sortCriteria sort order
     * @return the list of jobs.
     */
    public RESTResponse paginateResults(List<TAJob> tajobs, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = tajobs.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= tajobs.size())
            startIndex = pageLimit.get() * ((tajobs.size() - 1) / pageLimit.get());
        List<TAJob> paginatedJobs = Common.paginate(startIndex, maxRows, tajobs);
        response.setTotal(tajobs.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json artay
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }
}
