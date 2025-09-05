package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Course;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.AccessTimesService;
import services.CourseService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.generalError;

import javax.inject.Inject;

import static controllers.Application.checkLoginStatus;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class CourseController extends Controller {

    @Inject
    Config config;

    private final CourseService courseService;

    private final AccessTimesService accessTimesService;


    private Form<Course> courseFormTemplate;

    /******************************* Constructor **********************************************************************/
    @Inject
    public CourseController(FormFactory factory,
                                        AccessTimesService accessTimesService,
                                        CourseService courseService) {

        this.courseFormTemplate = factory.form(Course.class);
        this.courseService = courseService;
        this.accessTimesService = accessTimesService;

    }

    /************************************************** Course List *****************************************************/

    @With(OperationLoggingAction.class)
    public Result courseList() {
        checkLoginStatus();

        try {
            JsonNode coursesListNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.COURSE_LIST));

            if (coursesListNode.has("courses")) {
                for (JsonNode courseNode : coursesListNode.get("courses")) {
                    String courseId = courseNode.has("courseId") ? courseNode.get("courseId").asText() : "null";
                    Logger.debug("Course ID: " + courseId);
                    if ("null".equals(courseId)) {
                        Logger.error("Found course with null ID");
                    }
                }
            }

            return courseService.renderCourseListPage(coursesListNode, "all");
        } catch (Exception e) {
            Logger.debug("CourseController.courseList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /************************************************** Course List  **********************************************/

}
