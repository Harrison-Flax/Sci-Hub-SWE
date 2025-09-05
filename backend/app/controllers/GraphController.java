package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Author;
import models.GraphNode;
import models.Paper;
import play.mvc.Controller;
import play.mvc.Result;
import services.GraphService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class GraphController extends  Controller {
    private final GraphService graphService;

    @Inject
    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }
    /**
     * This method intends to get prime author connection in paper.
     *
     * @return created status with paper id created
     */
    public Result printPrimeConnections() {


            List<Paper> paperList = Paper.find.query().where().findList();

            List<List<Long>> primeConnections=graphService.criticalConnections(paperList);

            List<List<String>> authors = new ArrayList<>();
            for(List<Long> pConn  :primeConnections){
                List<String> authorNames=new ArrayList<>();
                for(Long con:pConn){
                    Author author = Author.find.query().where().eq("id", con).findOne();
                    authorNames.add(author.getFirstName());
                }
                authors.add(authorNames);
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", authors.toString());
            return ok(node);
    }

}
