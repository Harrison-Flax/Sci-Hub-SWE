package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import utils.Common;

import java.util.*;

import static utils.Constants.ACTIVE;

/**
 * This class intends to provide support for UserController.
 */
public class UserService {

    /**
     * Gets a list of users based on optional offset and pageLimit and sort criteria
     *
     * @param users        all users
     * @param offset       shows the start index of the users rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of users.
     */
    public RESTResponse paginateResults(List<User> users, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();

        int maxRows = users.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }

        /******************************* paginate the list ************************************************************/
        if (startIndex >= users.size())
            startIndex = pageLimit.get() * ((users.size() - 1) / pageLimit.get());
        List<User> paginatedUsers = Common.paginate(startIndex, maxRows, users);
        response.setTotal(users.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode usersNode = userList2JsonArray(paginatedUsers);

        response.setItems(usersNode);
        return response;
    }

    /**
     * Turn user list into json array
     *
     * @param userList list of users
     * @return json array of serialized users
     */
    public ArrayNode userList2JsonArray(List<User> userList) {
        ArrayNode usersNode = Json.newArray();
        for (User user : userList) {
            ObjectNode entryNode = (ObjectNode) Json.toJson(user);
            usersNode.add(entryNode);
        }
        return usersNode;
    }



}
