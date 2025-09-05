package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Author;
import models.ResearcherInfo;
import models.User;
import models.rest.RESTResponse;
import play.libs.Json;
import utils.Common;

import java.util.List;
import java.util.Optional;

/**
 * This class intends to provide support for AuthorController.
 */
public class AuthorService {

    /**
     * Gets a list of authors based on optional offset and pageLimit and sort criteria
     *
     * @param researchers        all researchers
     * @param offset       shows the start index of the authors rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of authors.
     */
//    public RESTResponse paginateResults(List<Author> authors, Optional<Integer> offset, Optional<Integer> pageLimit,
    public RESTResponse paginateResults(List<User> researchers, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();

        int maxRows = researchers.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }

        /******************************* paginate the list ************************************************************/
        if (startIndex >= researchers.size())
            startIndex = pageLimit.get() * ((researchers.size() - 1) / pageLimit.get());
//        List<Author> paginatedAuthors = Common.paginate(startIndex, maxRows, researchers);
        List<User> paginatedResearchers = Common.paginate(startIndex, maxRows, researchers);
        response.setTotal(researchers.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode authorsNode = authorList2JsonArray(paginatedResearchers);

        response.setItems(authorsNode);
        return response;
    }

    /**
     * Turn author list into json array
     *
     * @param authorList list of authors
     * @return json array of serialized authors
     */
//    public ArrayNode authorList2JsonArray(List<Author> authorList) {
    public ArrayNode authorList2JsonArray(List<User> authorList) {
        ArrayNode authorsNode = Json.newArray();
        for (User author : authorList) {
            ObjectNode entryNode = (ObjectNode) Json.toJson(author);
            authorsNode.add(entryNode);
        }
        return authorsNode;
    }



}
