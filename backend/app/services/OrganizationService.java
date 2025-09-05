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
 * @project: basicframework
 * @description: service supportive method for OrganizationController
 * @author: Junhao Shen
 * @date: 2022-05-22
 **/

public class OrganizationService {
    /**
     * Gets a list of organizations based on optional offset and pageLimit and sort criteria
     *
     * @param organizations        all organizations
     * @param offset       shows the start index of the organizations rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of organizations.
     */
    public RESTResponse paginateResults(List<Organization> organizations, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = organizations.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }

        /******************************* paginate the list ************************************************************/
        if (startIndex >= organizations.size())
            startIndex = pageLimit.get() * ((organizations.size() - 1) / pageLimit.get());
        List<Organization> paginatedOrganizations = Common.paginate(startIndex, maxRows, organizations);
        response.setTotal(organizations.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode organizationsNode = organizationList2JsonArray(paginatedOrganizations);

        response.setItems(organizationsNode);
        return response;
    }

    /**
     * Turn organization list into json array
     *
     * @param organizationList list of organizations
     * @return json array of serialized organizations
     */
    public ArrayNode organizationList2JsonArray(List<Organization> organizationList) {
        ArrayNode organizationsNode = Json.newArray();
        for (Organization organization : organizationList) {
            ObjectNode entryNode = (ObjectNode) Json.toJson(organization);
            organizationsNode.add(entryNode);
        }
        return organizationsNode;
    }
    
    
}
