package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Suggestion;
import models.rest.RESTResponse;
import play.Logger;
import utils.Common;

import java.util.List;
import java.util.Optional;

public class SuggestionService {
    /**
     * Gets a list of suggestions based on optional offset and pageLimit and sort criteria
     *
     * @param suggestionList     all suggestions
     * @param offset       shows the start index of the suggestion rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of suggestions.
     */
    public RESTResponse paginateResults(List<Suggestion> suggestionList, Optional<Integer> offset, Optional<Integer> pageLimit, String sortCriteria) throws Exception {
        try {
            RESTResponse response = new RESTResponse();

            int maxRows = suggestionList.size();
            if (pageLimit.isPresent()) {
                maxRows = pageLimit.get();
            }
            int startIndex = 0;

            if (offset.isPresent()) {
                startIndex = offset.get();
            }
            //*************************paginate the list ************************************************************

            if (startIndex >= suggestionList.size())
                startIndex = pageLimit.get() * ((suggestionList.size() - 1) / pageLimit.get());
            List<Suggestion> paginatedSuggestions = Common.paginate(startIndex, maxRows, suggestionList);
            response.setTotal(suggestionList.size());
            // Set the sortCriteria order.
            response.setSort(sortCriteria.split(" ")[0]);
            response.setOffset(startIndex);

            ArrayNode suggestionJsonArray = Common.objectList2JsonArray(suggestionList);
            response.setItems(suggestionJsonArray);
            return response;
        } catch (Exception e) {
            Logger.debug("SuggestionService.paginateResults() exception: " + e.toString());
            throw e;
        }
    }
}
