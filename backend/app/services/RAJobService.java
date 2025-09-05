package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Challenge;
import models.Job;
import models.RAJob;
import models.rest.RESTResponse;
import utils.Common;

import java.util.List;
import java.util.Optional;


public class RAJobService {
    /**
     * This method intends to return a list of RA jobs based on optional offset and pageLimit and sort criteria
     *
     * @param rajobs   all RA jobs
     * @param offset       shows the start index of the jobs rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of jobs.
     */
    public RESTResponse paginateResults(List<RAJob> rajobs, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = rajobs.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= rajobs.size())
            startIndex = pageLimit.get() * ((rajobs.size() - 1) / pageLimit.get());
        List<RAJob> paginatedJobs = Common.paginate(startIndex, maxRows, rajobs);
        response.setTotal(rajobs.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }
}
