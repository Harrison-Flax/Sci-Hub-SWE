package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Author;
import models.Paper;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.*;
import play.mvc.Result;
import services.AccessTimesService;
import services.PaperService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;

import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static utils.Constants.CALLER_IS_NOT_MY_SPACE_PAGE;

public class PaperController extends Controller {

    @Inject
    Config config;

    private final PaperService paperService;
    private final AccessTimesService accessTimesService;

    private Form<Paper> paperFormTemplate;
    private FormFactory myFactory;


    /******************************* Constructor **********************************************************************/
    @Inject
    public PaperController(FormFactory factory,
                           PaperService paperService,
                           AccessTimesService accessTimesService) {
        paperFormTemplate = factory.form(Paper.class);
        myFactory = factory;

        this.paperService = paperService;
        this.accessTimesService = accessTimesService;
    }


    /************************************************** Paper Registration ********************************************/
    /**
     * This method intends to render the paper registration page.
     *
     * @return
     */
    public Result paperRegisterPage() {
        checkLoginStatus();
        return ok(paperRegister.render());
    }

    /**
     *
     * @return
     */
    public Result loadUploadPage() {
        checkLoginStatus();
        return ok(loadPaper.render());
    }

    /**
     *
     * @return
     */
    public Result relationGraph() {
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.AUTHOR_PAPER_RELATION));
            JsonNode relationConnection = response.get("id");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(relationConnection);
            return ok(authorPaperRelation.render(json));
        } catch (Exception e) {
            Logger.debug("PaperController paper registration exception: " + e.toString());
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to gather paper registration information and create a paper in database.
     *
     * @return
     */
    public Result paperRegisterPOST() {
        checkLoginStatus();

        try {
            Form<Paper> paperForm = paperFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            ObjectNode jsonData = paperService.serializeFormToJson(paperForm);


            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.PAPER_REGISTER_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("PaperController.paperRegisterPOST: Cannot create the paper in backend");
                return ok(registrationError.render("Paper"));
            }

            long paperId = response.asLong();
            paperService.addAuthorsToPaper(paperForm, body, paperId);

            return ok(registerConfirmation.render(new Long(paperId), "Paper"));
        } catch (Exception e) {
            Logger.debug("PaperController paper registration exception: " + e.toString());
            return ok(registrationError.render("Paper"));
        }
    }
    /************************************************** End of Paper Registration ************************************/


    /************************************************** Paper List ***************************************************/

    /**
     * This method intends to prepare data for all papers.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for paperList.scala.html
     */
    public Result paperList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();


        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        int offset = pageLimit * (pageNum - 1);
        try {
            JsonNode usersJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.PAPER_LIST + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria));
            return paperService.renderPaperListPage(usersJsonNode, pageLimit, null,
                    "all", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("PaperController.paperList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Paper List ********************************************/

    /************************************************** Paper Detail *************************************************/
    /**
     * Ths method intends to return details of a paper. If a paper is not found, return to the all paper page (page 1?).
     *
     * @param paperId: paper id
     * @return: Paper, a list of team members to paperDetail.scala.html
     */
    public Result paperDetail(Long paperId) {
        try {
            Paper parentPaper = null;
            Paper paper = paperService.getPaperById(paperId);
            System.out.println("**size" + paper.getAuthors().size());
            if (paper == null) {
                Logger.debug("PaperController.paperDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            for (Author a : paper.getAuthors()) {
                System.out.println("**" + a.getEmail());
            }

            return ok(paperDetail.render(paper, null));
        } catch (Exception e) {
            Logger.debug("PaperController.paperDetail() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Paper Detail ******************************************/


    /**
     * This method intends to delete the paper by calling the backend
     *
     * @param paperId
     * @return redirect to the paper list page
     */
    public Result deletePaper(long paperId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_PAPER_BY_ID + paperId));
            //Todo We have to decide what to do if for some reason the paper could not get deactivated???
            return redirect(routes.PaperController.paperList(1, ""));
        } catch (Exception e) {
            Logger.debug("PaperController paper delete exception: " + e.toString());
            return redirect(routes.PaperController.paperList(1, ""));
        }
    }

    /**
     * This method intends to gather prime connection using graph.
     *
     * @return
     */
    public Result primeConnections() {
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PRIME_CONNECTIONS));
            JsonNode primeConnection = response.get("id");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(primeConnection);
            return ok(primeConnections.render(json));
        } catch (Exception e) {
            Logger.debug("PrimeConnection generate exception: " + e.toString());
            return ok(primeConnections.render(""));
        }

    }

    /**
     * This method is to process a DBLP file
     *
     * @return
     */
    public Result processDBPLFile() {
        try {

            paperService.storeDBLPSchema(request().body().asMultipartFormData());
            paperService.storeDBLP(request().body().asMultipartFormData());

        } catch (Exception e) {
            Logger.debug("PaperController DBLP file upload exception: " + e.toString());
            return ok("Please upload valid file");
        }

        return redirect(routes.PaperController.paperList(1, ""));
    }

    /**
     * This method is to process a Author Author Relation from paper-author table
     *
     * @return
     */
    public Result authorAuthorRel() {
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_AUTHOR_AUTHOR_REL));
            JsonNode authorConnection = response.get("id");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(authorConnection);
            return ok(authorAuthorRelation.render(json));
        } catch (Exception e) {
            Logger.debug("PrimeConnection generate exception: " + e.toString());
            return ok(authorAuthorRelation.render(""));
        }
    }

    /**
     * @TODO integrate LDA using cron job since java 8 is not supported by stanford NLP tmt
     *
     * @return
     */
    public Result paperLDA() {
        checkLoginStatus();
        try {

            JsonNode usersJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.LDA_TOPIC ));
            JsonNode LDAnode = usersJsonNode.get("id");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(LDAnode);
            json=json.replace("\n","</br>");
            return ok(rankedTopic.render(json));

        } catch (Exception e) {
            Logger.debug("PaperController.paperList() exception: " + e.toString());
            return ok(generalError.render());
        }
    }

    /**
     *
     * @return
     */
    public Result ldaTopicDistribution(){
        try{

        JsonNode topicsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.LDA_TOPIC_LIST ));
        JsonNode LDAnode = topicsJsonNode.get("id");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(LDAnode);
        json=json.replace("\n","</br>");
        return ok(LDAPieChart.render(json));

    } catch (Exception e) {
        Logger.debug("PaperController.paperList() exception: " + e.toString());
        return ok(generalError.render());
    }
}
    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(search.render("paper"));
    }

    /**
     *
     * @param pageNum
     * @param sortCriteria
     * @return
     */
    public Result searchPaper(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<Paper> tmpForm = paperFormTemplate.bindFromRequest();
            Map<String, String> tmpMap = tmpForm.data();


            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";



            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);


            JsonNode usersJsonNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.PAPER_SEARCH),searchJson);
            return paperService.renderPaperListPage(usersJsonNode, pageLimit,
                    searchString, "search", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("AuthorController.searchPOST exception: " + e.toString());
            return redirect(routes.Application.home());
        }

    }
}





