package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Reviewer;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.html.reviewerList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.isPrivateProjectZone;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;


/**
 * This class intends to provide support for ReviewerController.
 */
public class ReviewerService {
    @Inject
    Config config;

    /**
     * This method intends to create a json from a Reviewer form.
     *
     * @param userForm given user form
     * @return json of user's information
     */
    public ObjectNode createJsonFromReviewerForm(Form<Reviewer> userForm) {
        ObjectNode jsonData = null;
        try {
            Map<String, String> tmpMap = userForm.data();
            jsonData = (ObjectNode)(Json.toJson(tmpMap));

            StringBuffer userName = new StringBuffer();
            userName.append(userForm.field("firstName").value());
            userName.append(" ");
            if (!userForm.field("middleInitial").value().equals("")) {
                userName.append(userForm.field("middleInitial").value());
                userName.append(" ");
            }
            userName.append(userForm.field("lastName").value());
            jsonData.put("userName", userName.toString());
        } catch (Exception e) {
            Logger.debug("ServiceService.generateJsonNodeFromForm exception: " + e.toString());
            throw e;
        }
        return jsonData;
    }

    /**
     * This method renders the user list page.
     * @param userListJsonNode
     * @param isCallerMySpacePage shows if the user list page is rendered from my space page or not
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "my follow"; "user publish"; "search"
     * @param username
     * @param userId
     * @return render user list page; If exception happened render homepage
     */
    public Result renderReviewerListPage(JsonNode userListJsonNode, boolean isCallerMySpacePage, int pageLimit,
                                         String searchBody, String listType, String username, Long userId) {
        try {
            if (userListJsonNode == null || userListJsonNode.has("error")) {
                Logger.debug("User list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode userJsonArray = userListJsonNode.get("items");

            if (!userJsonArray.isArray()) {
                Logger.debug("User list is not array!");
                return redirect(routes.Application.home());
            }


            List < Reviewer > users = new ArrayList < > ();


            for (int i = 0; i < userJsonArray.size(); i++) {
                JsonNode json = userJsonArray.path(i);
                Reviewer user = Reviewer.deserialize(json);
                users.add(user);
            }

            // Offset
            String retSort = userListJsonNode.get("sort").asText();
            int total = userListJsonNode.get("total").asInt();
            int count = userListJsonNode.get("count").asInt();
            int offset = userListJsonNode.get("offset").asInt();
            int page = offset / pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);
            return ok(reviewerList.render(users,
                    isPrivateProjectZone(), isCallerMySpacePage, page, retSort, offset, total, count, listType,
                    pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("AuthorService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


}