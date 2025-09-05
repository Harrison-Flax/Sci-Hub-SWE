package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Challenge;
import models.Technology;
import models.rest.RESTResponse;
import play.libs.Json;
import utils.Common;

import java.util.List;
import java.util.Optional;


public class TechnologyService {
    /**
     * This method intends to return a list of projects based on optional offset and pageLimit and sort criteria
     *
     * @param technologies   all technologies
     * @param offset       shows the start index of the technologies rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of technologies.
     */
    public RESTResponse paginateResults(List<Technology> technologies, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = technologies.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= technologies.size())
            startIndex = pageLimit.get() * ((technologies.size() - 1) / pageLimit.get());
        List<Technology> paginatedTechnologies = Common.paginate(startIndex, maxRows, technologies);
        response.setTotal(technologies.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode technologiesNode = Common.objectList2JsonArray(paginatedTechnologies);
        response.setItems(technologiesNode);
        return response;
    }
}
