package services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.ExpressionList;
import play.api.Play;
import models.Author;
import models.AuthorPaper;
import models.Paper;
import models.rest.RESTResponse;
import utils.Common;
import play.Logger;
import utils.Constants;
import utils.RESTfulCalls;
import play.libs.ws.*;

import java.io.*;
import java.util.*;

/**
 * This class intends to provide support for ProjectController.
 * TODO: Project Controller should be changed to ProjectController.
 */
public class PaperService {

    /**
     * This method intends to return a list of papers based on optional offset and pageLimit and sort criteria
     *
     * @param projects     all projects
     * @param offset       shows the start index of the projects rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of projects.
     */
    public RESTResponse paginateResults(List<Paper> projects, Optional<Integer> offset, Optional<Integer> pageLimit,
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
        List<Paper> paginatedPapers = Common.paginate(startIndex, maxRows, projects);
        response.setTotal(projects.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode projectsNode = Common.objectList2JsonArray(paginatedPapers);
        response.setItems(projectsNode);
        return response;
    }

    public String constructPaperAuthorRelation(int[][] relationMatrix,Map<Long,List<Author>> mapA) throws
            JsonProcessingException{

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode childNode1 = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode childNode2 = mapper.createObjectNode();
        ArrayNode arrayNode2 = mapper.createArrayNode();
        ObjectNode parent = mapper.createObjectNode();
        try {

            int paperCount=mapA.size()-1;
            for (Long paperId : mapA.keySet()) {
                childNode1 = mapper.createObjectNode();
                if(paperId>1) {
                    if (Paper.find.query().where().eq("id", paperId - 1).findOne() != null) {
                        childNode1.put("title", Paper.find.query().where().eq("id",
                                paperId - 1).findOne().getTitle());
                    }
                }
                childNode1.put("name", paperId);
                childNode1.put("entity", "Paper");
                arrayNode.add(childNode1);
            }
            int countG=0;
            for (Long paperId : mapA.keySet()) {
                for (Author author : mapA.get(paperId)) {
                    childNode2 = mapper.createObjectNode();
                    childNode2.put("source", paperId);
                    childNode2.put("target", author.getId()+paperCount);
                    childNode2.put("value", countG++);
                    childNode2.put("size",countG);
                    arrayNode2.add(childNode2);
                }
            }
            int count=0;
            for (int i=0;i<relationMatrix.length-1;i++) {
                childNode1 = mapper.createObjectNode();
                String authorName = null;
                if (Author.find.query().where().eq("id", i+1).findOne() != null) {

                    authorName = Author.find.query().where().eq("id", i + 1).findOne().
                            getAuthorName();
                }
                if(authorName.split(" ").length>1)
                    authorName=authorName.split(" ")[1];
                else
                    authorName=authorName.split(" ")[0];
                childNode1.put("title",authorName);
                int q=i+1;
                childNode1.put("name",q+paperCount);
                childNode1.put("entity","Author");
                arrayNode.add(childNode1);
            }
            rootNode.put("nodes",arrayNode);
            for (int i=1;i<relationMatrix.length;i++) {
                count=0;
                for (int j=1;j<relationMatrix[0].length;j++) {
                    if (relationMatrix[i][j] == 1) {
                        count++;
                        childNode2 = mapper.createObjectNode();
                        childNode2.put("source", i+paperCount);
                        childNode2.put("target", j+paperCount);
                        childNode2.put("value", countG++);
                        childNode2.put("size",count);
                        arrayNode2.add(childNode2);
                    }
                }
            }

            rootNode.put("links",arrayNode2);
            parent.set("id",rootNode);
        }
        catch(Exception e) {
            e.printStackTrace();
            Logger.debug("paperService.constructd3data() exception: " + e.toString());

        }
        System.out.println(mapper.writeValueAsString(parent));
        return mapper.writeValueAsString(parent);
    }

    public String getAbstract(String title){

        JsonNode resultSet=RESTfulCalls.getAPI("http://api.aminer.org/api/search/pub?query="+title);

        if (resultSet != null && resultSet.get("result")!=null && resultSet.get("result").size() > 0) {
            JsonNode resultArr = resultSet.get("result");
            if (resultArr != null && resultArr.size() > 0) {
                JsonNode result = resultArr.get(0);
                if (result != null && result.size() > 0) {
                    JsonNode highlight = result.get("highlight");
                    if (highlight != null && highlight.size() > 0) {
                        return highlight.get("abstract").toString();
                    }
                }
            }
        }
       return "";
    }

    public String constructAuthorRelation(int[][] relationMatrix) throws JsonProcessingException{

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode childNode1 = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode childNode2 = mapper.createObjectNode();
        ArrayNode arrayNode2 = mapper.createArrayNode();
        ObjectNode parent = mapper.createObjectNode();

        try {
            int count=0;
            for (int i=0;i<relationMatrix.length-1;i++) {
                childNode1 = mapper.createObjectNode();
                if(Author.find.query().where().eq("id", i+1).findOne()!=null)
                childNode1.put("label",Author.find.query().where().eq("id", i+1).findOne().
                        getAuthorName());
                childNode1.put("id",i+1);
                childNode1.put("group",i+1);
                count=0;
                for (int j=0;j<relationMatrix[0].length-1;j++) {
                    if (relationMatrix[i][j] == 1||relationMatrix[j][i]==1)
                    count++;
                }
                childNode1.put("value",count);
                arrayNode.add(childNode1);
            }
            rootNode.put("nodes",arrayNode);
            for (int i=1;i<relationMatrix.length;i++) {
                for (int j=1;j<relationMatrix[0].length;j++) {
                    if (relationMatrix[i][j] == 1) {
                        childNode2 = mapper.createObjectNode();
                        childNode2.put("from", i);
                        childNode2.put("to", j);
                        childNode2.put("value", 10);
                        arrayNode2.add(childNode2);
                    }
                }
            }
            rootNode.put("links",arrayNode2);

            parent.set("id",rootNode);
        }
        catch(Exception e) {
            e.printStackTrace();
            Logger.debug("PaperService.constructAuthorRelation() exception: " + e.toString());

        }
        System.out.println(mapper.writeValueAsString(parent));
        return mapper.writeValueAsString(parent);
    }


    public String getLDATopicDistibution() throws  JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode childNode = mapper.createObjectNode();
        ObjectNode childNode1 = mapper.createObjectNode();
        ObjectNode parent = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode childNode2 = mapper.createObjectNode();
        ArrayNode arrayNode2 = mapper.createArrayNode();

        List<String> outputTerms = new ArrayList<String>();

        try {
            BufferedReader topicsReader = new BufferedReader(new FileReader(new File
                    ("LDA/out.csv-top-terms.csv")));
            String line = topicsReader.readLine();
            HashMap<String,Integer> topicMap=new HashMap<>();
            ArrayList<String> topicList=new ArrayList<>();
            while ((line = topicsReader.readLine()) != null) {
                topicList.add(line);
                topicMap.put(line,0);
            }
//            var data = [
//            {
//                key: 1,
//                        values: [
//                {
//                    Topic1: 20,
//                            Topic2: 30      }
//                    ]
//            }
//            ]30
            BufferedReader extractedFileReader = new BufferedReader(new FileReader(new File
                    ("LDA/ExtractedByTermsFile.txt")));
            while ((line = extractedFileReader.readLine()) != null) {
                for(String topic:topicList) {
                    if(line.contains(topic)) {
                        int count = topicMap.get(topic)+1;
                        topicMap.put(topic, count);
                    }
                }
            }
            for(String topic: topicMap.keySet()){
                childNode =  mapper.createObjectNode();
                childNode.put("label",topic);
            childNode.put("count",topicMap.get(topic));
                arrayNode.add(childNode);
            }


            parent.set("id",arrayNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(mapper.writeValueAsString(parent));
        return mapper.writeValueAsString(parent);
    }



}
