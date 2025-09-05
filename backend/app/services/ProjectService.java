package services;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Project;
import models.rest.RESTResponse;
import play.libs.Json;
import utils.Common;

import java.util.List;
import java.util.Optional;

/**
 * This class intends to provide support for ProjectController.
 * TODO: Project Controller should be changed to ProjectController.
 */
public class ProjectService {

    /**
     * This method intends to return a list of projects based on optional offset and pageLimit and sort criteria
     *
     * @param projects     all projects
     * @param offset       shows the start index of the projects rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of projects.
     */
    public RESTResponse paginateResults(List<Project> projects, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = projects.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= projects.size())
            startIndex = pageLimit.get() * ((projects.size() - 1) / pageLimit.get());
        List<Project> paginatedProjects = Common.paginate(startIndex, maxRows, projects);
        response.setTotal(projects.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode projectsNode = Common.objectList2JsonArray(paginatedProjects);
        response.setItems(projectsNode);
        return response;
    }




}
