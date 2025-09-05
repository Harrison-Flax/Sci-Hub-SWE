package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Course;
import models.rest.RESTResponse;
import utils.Common;

import java.util.List;
import java.util.Optional;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class CourseService {

    public RESTResponse paginateResults(List<Course> courses, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = courses.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= courses.size())
            startIndex = pageLimit.get() * ((courses.size() - 1) / pageLimit.get());
        List<Course> paginateCourses = Common.paginate(startIndex, maxRows, courses);
        response.setTotal(courses.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json artay
        ArrayNode jobsNode = Common.objectList2JsonArray(paginateCourses);
        response.setItems(jobsNode);
        return response;
    }
}
