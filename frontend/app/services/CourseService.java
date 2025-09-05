package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Course;
import play.Logger;
import play.mvc.Result;
import views.html.taHoursRecords;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class CourseService {
    @Inject
    Config config;


    public Result renderCourseListPage(JsonNode coursesListNode, String listType) throws Exception {
        if (coursesListNode != null && coursesListNode.has("error")) {
            Logger.debug("Error in provided course list!");
            return redirect(routes.Application.home());
        }

        List<Course> courses = new ArrayList<>();
        if (coursesListNode != null && coursesListNode.isArray()) {
            for (int i = 0; i < coursesListNode.size(); i++) {
                JsonNode json = coursesListNode.path(i);
                Course course = Course.deserialize(json);
                courses.add(course);
            }
        } else {
            Logger.debug("Course list is empty or not an array, rendering empty list.");
        }

        return ok(taHoursRecords.render(courses, listType));
    }



}
