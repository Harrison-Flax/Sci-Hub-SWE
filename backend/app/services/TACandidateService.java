package services;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.TACandidate;
import models.rest.RESTResponse;
import utils.Common;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class TACandidateService {
    /**
     * This method intends to return a list of TA jobs based on optional offset and pageLimit and sort criteria
     *
     * @patam tajobs   all TA jobs
     * @patam offset       shows the start index of the jobs rows we want to receive
     * @patam pageLimit    shows the number of rows we want to receive
     * @patam sortCriteria sort order
     * @return the list of jobs.
     */
    public RESTResponse paginateResults(List<TACandidate> taCandidates, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = taCandidates.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= taCandidates.size())
            startIndex = pageLimit.get() * ((taCandidates.size() - 1) / pageLimit.get());
        List<TACandidate> paginatedJobs = Common.paginate(startIndex, maxRows, taCandidates);
        response.setTotal(taCandidates.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json artay
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }


}
