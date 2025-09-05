package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.ExpressionList;
import models.Author;
import models.Reviewer;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.ReviewerService;
import utils.Common;
import play.libs.Json;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReviewerController extends Controller {

    public static final String REVIEWER_DEFAULT_SORT_CRITERIA = "reviewer_name";
    @Inject
    Config config;

    private final ReviewerService reviewerService;

    @Inject
    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }


    static private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /********************************************* Add Reviewer *******************************************************/
    /**
     * Registers a new reviewer provided the reviewer data in the request body.
     *
     * @return the reviewer id of the reviewer created.
     */
    public Result addReviewer() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("Reviewer not created, expecting Json data");
        }

        final Reviewer reviewer = Json.fromJson(json, Reviewer.class);
        reviewer.setReviewerName(reviewer.getFirstName() + " " + reviewer.getLastName());
        reviewer.setCreateTime(new Date().toString());

        try {
            if ((Reviewer.find.query().where().eq("email", reviewer.getEmail()).findList()).size() != 0) {
                return Common.badRequestWrapper("Email has been used");
            }

            reviewer.save();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", reviewer.getId());
            return ok(node);
        } catch (Exception e) {
            Logger.debug("ReviewerController.addReviewer() exception " + e.toString());
            return notFound("Reviewer not added");
        }
    }
    /********************************************* End of Add Reviewer ************************************************/


    /********************************************* Update Reviewer ****************************************************/
    /**
     * This method receives updated information about reviewer and saves the changes
     *
     * @return ok or bad request
     */
    public Result updateReviewer() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                return Common.badRequestWrapper("Cannot update reviewer, expecting Json data");
            }

            Reviewer updatedReviewer = Json.fromJson(json, Reviewer.class);
            updatedReviewer.setReviewerName(Reviewer.createReviewerName(updatedReviewer.getFirstName(), updatedReviewer.
                    getMiddleInitial(), updatedReviewer.getLastName()));

            updatedReviewer.update();

            return ok("updated");
        } catch (Exception e) {
            Logger.debug("ReviewerController.updateReviewer exception: " + e.toString());
            return internalServerError("User could not be updated.");
        }
    }
    /********************************************* End of Update Reviewer *********************************************/

    /********************************************* Reviewer Detail ****************************************************/
    /**
     * This method intends to returns a reviewer's information from a reviewer id.
     *
     * @param reviewerId reviewer reviewerId
     * @return reviewer
     */
    public Result reviewerDetail(Long reviewerId) {
        if (reviewerId == null) {
            Logger.debug("Reviewer reviewerId is null or empty for ReviewerController.reviewerDetail");
            return Common.badRequestWrapper("Reviewer is not valid");
        }

        try {
            Reviewer reviewer = Reviewer.find.query().where().eq("id", reviewerId).findOne();
            if (reviewer == null) {
                Logger.info("Reviewer not found with reviewerId: " + reviewerId);
                return notFound("Reviewer not found with reviewerId: " + reviewerId);
            }
            JsonNode jsonNode = Json.toJson(reviewer);
            String result = jsonNode.toString();
            return ok(result);
        } catch (Exception e) {
            Logger.debug("ReviewerController.reviewerDetail exception: " + e.toString());
            return notFound("Reviewer was not found.");
        }
    }

    /********************************************* End of Reviewer Login **********************************************/

    /********************************************* Reviewer List ******************************************************/
    /**
     * Gets a list of all the reviewers based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param offset       shows the start index of the rows we want to receive
     * @param sortCriteria shows based on what column we want to sort the data
     * @return the list of reviewers.
     */
    public Result reviewerList(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
        List<Reviewer> reviewers = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, REVIEWER_DEFAULT_SORT_CRITERIA);
        try {
            reviewers = Reviewer.find.query().orderBy(sortOrder).findList();
            RESTResponse response = reviewerService.paginateResults(reviewers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("ReviewerController.reviewerList exception: " + e.toString());
            return notFound("Reviewer not found");
        }
    }


    /***************************************** End of Reviewer List ***************************************************/

    /***************************************** Search Reviewer List ***************************************************/

    /**
     * Find reviewer by multiple condition, including reviewername, affiliation, email, mailing address, phone number,
     * research area, etc.
     *
     * @return reviewers that match the condition
     */
    public Result searchReviewerByCondition(Optional<Integer> pageLimit, Optional<Integer> offset,
                                            Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, REVIEWER_DEFAULT_SORT_CRITERIA);
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.debug("Reviewer cannot be queried, expecting Json data");
            return badRequest("Reviewer cannot be queried, expecting Json data");
        }
        try {
            String reviewername = json.get("name").asText();
            String affiliation = json.get("Affiliation").asText();
            String email = json.get("Email").asText();
            String mailing_address = json.get("MailingAdd").asText();
            String phone = json.get("PhoneNum").asText();
            String research_area = json.get("Research Area").asText();
            //Search reviewer by conditions
            ExpressionList<Reviewer> query = Reviewer.find.query().where().
                    icontains("affiliation", affiliation.toLowerCase()).
                    icontains("email", email.toLowerCase()).
                    icontains("mailing_address", mailing_address.toLowerCase()).
                    icontains("phone_number", phone.toLowerCase()).
                    icontains("research_fields", research_area.toLowerCase());

            String[] names = reviewername.split(" ");
            for (String name : names) {
                query = query.icontains("reviewer_name", name.toLowerCase());
            }

            List<Reviewer> reviewers = query.orderBy(sortOrder).findList();

            RESTResponse response = reviewerService.paginateResults(reviewers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("Reviewer cannot be queried, query is corrupted");
            return badRequest("Reviewer cannot be queried, query is corrupted");
        }
    }


    /************************************** End of Search Reviewer List ***********************************************/


    /****************************************** Sort Reviewer List ****************************************************/


    /**
     * Sort the given list of reviewers
     *
     * @param reviewers    List of reviewers to be sorted
     * @param sortCriteria sort criteria
     * @return sorted list of reviewers.
     */
    public static void sortReviewers(List<Reviewer> reviewers, String sortCriteria) {
        if (sortCriteria.equals("reviewer_name")) {
            Comparator<Reviewer> com = new Comparator<Reviewer>() {
                @Override
                public int compare(Reviewer o1, Reviewer o2) {
                    String name1 = o1.getReviewerName();
                    String name2 = o2.getReviewerName();
                    return name1.toLowerCase().compareTo(name2.toLowerCase());
                }
            };
            Collections.sort(reviewers, com);
        } else {
            Comparator<Reviewer> com = new Comparator<Reviewer>() {
                @Override
                public int compare(Reviewer o1, Reviewer o2) {
                    return new Long(o1.getId()).compareTo(new Long(o2.getId()));
                }
            };
            Collections.sort(reviewers, com);
        }
    }
    /************************************** End of Search Reviewer List ***********************************************/


}
