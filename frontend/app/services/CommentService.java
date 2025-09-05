package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import utils.Constants;
import utils.RESTfulCalls;

public class CommentService {

    private final static String CREATEW = Constants.URL_HOST + Constants.CMU_BACKEND_PORT + "/workflow/addComment";
    private final static String CREATESL = Constants.URL_HOST + Constants.CMU_BACKEND_PORT +
            "/serviceExecutionLog/addComment";
    private final static String CREATES = Constants.URL_HOST + Constants.CMU_BACKEND_PORT + "/service/addComment";
    private final static String CREATED = Constants.URL_HOST + Constants.CMU_BACKEND_PORT + "/docker/addComment";
    private static final String ADD_COMMENT_NOTEBOOK = Constants.URL_HOST + Constants.CMU_BACKEND_PORT +
            Constants.ADD_COMMENT_FOR_NOTEBOOK;
    private static final String ADD_COMMENT_DATASET = Constants.URL_HOST + Constants.CMU_BACKEND_PORT +
            Constants.ADD_DATASET_COMMENT;


    /**
     * This method tries to create a comment to various pages.
     * @param node
     * @return
     */
    public static JsonNode createComment(ObjectNode node) throws Exception {
        JsonNode response = null;
        try {
            if (node.path("workflowID") != null &&
                    !node.path("workflowID").asText().equals("")) {
                response = RESTfulCalls.postAPI(CREATEW, node);
            } else if (node.path("serviceLogID") != null &&
                    !node.path("serviceLogID").asText().equals("")) {
                response = RESTfulCalls.postAPI(CREATESL, node);
            } else if (node.path("serviceID") != null &&
                    !node.path("serviceID").asText().equals("")) {
                response = RESTfulCalls.postAPI(CREATES, node);
            } else if (node.path("dockerId") != null &&
                    !node.path("dockerId").asText().equals("")) {
                response = RESTfulCalls.postAPI(CREATED, node);
            } else if (node.path("notebookId") != null &&
                    !node.path("notebookId").asText().equals("")) {
                response = RESTfulCalls.postAPI(ADD_COMMENT_NOTEBOOK, node);
            } else if (node.path("datasetEntryId") != null &&
                    !node.path("datasetEntryId").asText().equals("")) {
                response = RESTfulCalls.postAPI(ADD_COMMENT_DATASET, node);
            }
        } catch (Exception e) {
            Logger.debug("CommentService.createComment() exception: " + e.toString());
            throw e;
        }
        return response;
    }
}
