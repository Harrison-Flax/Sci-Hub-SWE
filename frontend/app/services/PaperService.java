package services;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Paper;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.*;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.paperList;

import java.io.File;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;
import static controllers.Application.isPrivateProjectZone;

/**
 * This class intends to provide support for PaperController.
 */
public class PaperService {
    @Inject
    Config config;


    private Form<Paper> projectForm;

    private final AuthorService authorService;

    @Inject
    public PaperService(AuthorService authorService) {
        this.authorService = authorService;
    }


    /**
     * This method intends to get Paper by id by calling backend APIs.
     *
     * @param projectId
     * @return Paper
     */
    public Paper getPaperById(Long paperId) {
        Paper paper = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PAPER_BY_ID + paperId));
            if (response.has("error")) {
                Logger.debug("PaperService.getPaperById() did not get paper from backend with error.");
                return null;
            }

            paper = Paper.deserialize(response);
        } catch (Exception e) {
            Logger.debug("PaperService.getPaperById() exception: " + e.toString());
            return null;
        }
        return paper;
    }


    /**
     * This method intends to add a list of team members to a paper, from paper registration form.
     *
     * @param paperForm: paper registration form
     * @param body
     * @param paperId:   paper id
     */
    public void addAuthorsToPaper(Form<Paper> paperForm, Http.MultipartFormData body, Long paperId) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            int count = Integer.parseInt(paperForm.field("count").value());

            //the number of authors in the paper
            for (int i = 0; i < count; i++) {
                if (paperForm.field("member" + i) != null) {
                    ObjectNode memberData = mapper.createObjectNode();
                    memberData.put("name", paperForm.field("member" + i).value());
                    memberData.put("email", paperForm.field("email" + i).value());
                    JsonNode memberRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.ADD_AUTHOR_TO_PAPER + paperId), memberData);
                }
            }
        } catch (Exception e) {
            Logger.debug("PaperService.addTeamMembersToPaper exception: " + e.toString());
            throw e;
        }
    }

    public void storeDBLP(Http.MultipartFormData body) throws Exception {
        try {
            int count = 0;
            FilePart xml=null;
            List<FilePart> fileParts = body.getFiles();
            for(FilePart filePart : fileParts) {
                if(++count==1) {
                    xml = (FilePart)filePart;
                    break;
                }
            }
            StringBuilder stringBuilderXml = readFile(xml);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode memberData = mapper.createObjectNode();
            memberData.put("data", stringBuilderXml.toString());
            memberData.put("name",xml.getFilename());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.POST_PAPER_FROM_DBLP), memberData);
        } catch (Exception e) {
            Logger.debug("ProjectService.savePDFToProject exception: " + e.toString());
            throw e;
        }
    }

    public void storeDBLPSchema(Http.MultipartFormData body) throws Exception {

        try {
            int count = 0;
            FilePart schema=null;
            List<FilePart> fileParts = body.getFiles();
            for(FilePart filePart : fileParts) {
                if(++count==2) {
                    schema = (FilePart)filePart;
                    break;
                }
            }
            System.out.println(schema);
            StringBuilder stringBuilderXml = readFile(schema);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode memberData = mapper.createObjectNode();
            memberData.put("data", stringBuilderXml.toString());
            memberData.put("name",schema.getFilename());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.LOAD_SCHEMA), memberData);
        } catch (Exception e) {
            Logger.debug("ProjectService.savePDFToProject exception: " + e.toString());
            throw e;
        }
    }


    private StringBuilder readFile(Http.MultipartFormData.FilePart filePart) throws IOException{
        StringBuilder stringBuilderXml = null;
        if (filePart != null && !filePart.getFilename().equals("")) {
            FileReader fr = new FileReader((File) filePart.getFile());
            BufferedReader reader = new BufferedReader(fr);
            stringBuilderXml = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilderXml.append(line);
            }
        }
        return stringBuilderXml;
    }


    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the paper list page.
     * Note that for performance consideration, the backend only passes back the papers for the needed page stored in
     * the paperListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param paperListJsonNode
     * @param currentPaperZone
     * @param pageLimit
     * @param searchBody
     * @param listType          : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render paper list page; If exception happened then render the homepage
     */
    public Result renderPaperListPage(JsonNode paperListJsonNode,

                                      int pageLimit,
                                      String searchBody,
                                      String listType,
                                      String username,
                                      Long userId) {
        try {
            if (paperListJsonNode == null || paperListJsonNode.has("error")) {
                Logger.debug("User list is empty or error!");
                return redirect(routes.Application.home());
            }
            JsonNode userJsonArray = paperListJsonNode.get("items");
            if (!userJsonArray.isArray()) {
                Logger.debug("User list is not array!");
                return redirect(routes.Application.home());
            }
            List<Paper> users = new ArrayList<>();
            for (int i = 0; i < userJsonArray.size(); i++) {
                JsonNode json = userJsonArray.path(i);
                Paper user = Paper.deserialize(json);
                users.add(user);
            }
            // Offset
            String retSort = paperListJsonNode.get("sort").asText();
            int total = paperListJsonNode.get("total").asInt();
            int count = paperListJsonNode.get("count").asInt();
            int offset = paperListJsonNode.get("offset").asInt();
            int page = offset / pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);
            System.out.println(users);
            return ok(paperList.render(users, page, retSort, offset, total, count, listType, pageLimit,
                    searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("PaperService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    /**
     * This method intends to prepare a json object from Paper form.
     *
     * @param paperForm: paper registration form
     * @return
     * @throws Exception
     */
    public ObjectNode serializeFormToJson(Form<Paper> paperForm) throws Exception {
        ObjectNode jsonData = null;
        try {
            Map<String, String> tmpMap = paperForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));
        } catch (Exception e) {
            Logger.debug("PaperService.serializeFormToJson exception: " + e.toString());
            throw e;
        }

        return jsonData;
    }

}