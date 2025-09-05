package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Reviewer;
import models.rest.RESTResponse;
import play.libs.Json;
import utils.Common;

import java.util.List;
import java.util.Optional;

/**
 * This class intends to provide support for ReviewerController.
 */
public class ReviewerService {

    /**
     * Gets a list of reviewers based on optional offset and pageLimit and sort criteria
     *
     * @param reviewers        all reviewers
     * @param offset       shows the start index of the reviewers rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of reviewers.
     */
    public RESTResponse paginateResults(List<Reviewer> reviewers, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();

        int maxRows = reviewers.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }

        /******************************* paginate the list ************************************************************/
        if (startIndex >= reviewers.size())
            startIndex = pageLimit.get() * ((reviewers.size() - 1) / pageLimit.get());
        List<Reviewer> paginatedAuthors = Common.paginate(startIndex, maxRows, reviewers);
        response.setTotal(reviewers.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode reviewersNode = reviewerList2JsonArray(paginatedAuthors);

        response.setItems(reviewersNode);
        return response;
    }

    /**
     * Turn reviewer list into json array
     *
     * @param reviewerList list of reviewers
     * @return json array of serialized reviewers
     */
    public ArrayNode reviewerList2JsonArray(List<Reviewer> reviewerList) {
        ArrayNode reviewersNode = Json.newArray();
        for (Reviewer reviewer : reviewerList) {
            ObjectNode entryNode = (ObjectNode) Json.toJson(reviewer);
            reviewersNode.add(entryNode);
        }
        return reviewersNode;
    }



}
