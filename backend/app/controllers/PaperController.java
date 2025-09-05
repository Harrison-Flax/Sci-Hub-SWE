package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.ExpressionList;
import models.*;
import models.rest.RESTResponse;
import org.xml.sax.InputSource;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;
import utils.Common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import java.io.*;
import java.io.File;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



public class PaperController extends Controller {
    public static final String PAPER_DEFAULT_SORT_CRITERIA = "title";

    private final PaperService paperService;

    @Inject
    public PaperController(PaperService paperService) {
        this.paperService = paperService;

    }

    /************************************************* Add Paper ******************************************************/
    /**
     * This method intends to add a paper into knowledge graph.
     *
     * @return created status with paper id created
     */
    public Result addPaper() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Paper information not saved, expecting Json data");
                return badRequest("Paper information not saved, expecting Json data");
            }

            Paper paper = Json.fromJson(json, Paper.class);

            // If paper title, publicationChannel, and pages are exactly the same, it means the paper exists in the
            // database.
            if (((Paper.find.query().where().eq("title", paper.getTitle()).findList()).size() != 0)) {
                Paper paperExisting = Paper.find.query().where().eq("title", paper.getTitle()).findList().
                        get(0);
                if (paper.getPublicationChannel().equalsIgnoreCase(paperExisting.getPublicationChannel()) &&
                        paper.getPages().equals(paperExisting.getPages())) {
                    return Common.badRequestWrapper("Paper exists and cannot be added again");
                }
            }

            paper.save();
            return ok(Json.toJson(paper.getId()).toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Paper cannot be added: " + e.toString());
            return badRequest("Paper not saved: ");
        }
    }
    /************************************************* End of Add Paper ***********************************************/


    /************************************************* Delete Paper ***************************************************/
    /**
     * This method receives a paper id and deletes the paper.
     *
     * @param paperId given paper Id
     * @return ok or not found
     */
    public Result deletePaper(Long paperId) {
        try {
            Paper paper = Paper.find.byId(paperId);
            if (paper == null) {
                Logger.debug("In PaperController deletePaper(), cannot find paper with id: " + paperId);
                return notFound("From backend PaperController, Paper not found with id: " + paperId);
            }

            paper.delete();
            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Paper cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete paper for id: " + paperId);
        }
    }

    /************************************************* End of Delete Paper ********************************************/


    /************************************************* Add Author for Paper *******************************************/
    /**
     * This method intends to add an author for a paper.
     *
     * @param paperId
     */
    public Result addAuthor(Long paperId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Author not saved, expecting Json data");
                return badRequest("Author not saved, expecting Json data");
            }

            Paper paper = new Paper(paperId);

            String name = json.findPath("name").asText();
            String email = json.findPath("email").asText();
            Author author = null;

                author = Author.find.query().where().eq("email", email).findOne();

            // If the team member does not have an account in OpenNEX
            if (author == null) {
                author = new Author(name, email);

                List<Paper> paperList = new ArrayList<Paper>();
                paperList.add(paper);
                author.setPapersByAuthor(paperList);
                author.save();
            } else {
                List<Paper> paperList = author.getPapersByAuthor();
                if (paperList == null) {
                    paperList = new ArrayList<Paper>();
                    paperList.add(paper);
                    author.setPapersByAuthor(paperList);
                } else {
                    if (!paperList.contains(paper))
                        paperList.add(paper);
                }
                author.update();
            }

            return created(Json.toJson(author.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Cannot add team member for paper: " + e.toString());
            return badRequest("Team Member not saved.");
        }
    }


    /******************************************** End of Add Author for Paper *****************************************/


    /************************************************* Paper List *****************************************************/
    /**
     * Gets a list of all the papers based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of papers
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result paperList(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {

        List<Paper> papers = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, PAPER_DEFAULT_SORT_CRITERIA);

        try {
            JsonNode json = request().body().asJson();
            System.out.println("****"+json);


                papers = Paper.find.query().orderBy(sortOrder).findList();


            RESTResponse response = paperService.paginateResults(papers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.paperList() exception: " + e.toString());
            return internalServerError("PaperController.paperList() exception: " + e.toString());
        }
    }

    /**
     * Gets a list of all the papers based on optional offset and limit and sort
     *
     * @return a list of papers
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result paperSearchList() {

        List<Paper> papers = new ArrayList<>();
        String sortOrder = PAPER_DEFAULT_SORT_CRITERIA;

        try {
            JsonNode json = request().body().asJson();
            System.out.println("****"+json);
            if (json != null) {


                String title = json.get("Title").asText();
                String bookTitle = json.get("BookTitle").asText();
                String abstractText = json.get("AbstractText").asText();
                String publicationType = json.get("PublicationType").asText();
                String publicationChannel = json.get("PublicationChannel").asText();

                //Search user by conditions
                ExpressionList<Paper> query = Paper.find.query().where().
                        icontains("title", title.toLowerCase()).
                        icontains("book_title", bookTitle.toLowerCase()).
                        icontains("abstract_text", abstractText.toLowerCase()).
                        icontains("publication_type", publicationType.toLowerCase()).
                        icontains("publication_channel", publicationChannel.toLowerCase());


                papers=query.orderBy(sortOrder).findList();
            }
            Optional<Integer> offset = Optional.ofNullable(0);
            Optional<Integer> pageLimit = Optional.ofNullable(20);

            RESTResponse response = paperService.paginateResults(papers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.paperList() exception: " + e.toString());
            return internalServerError("PaperController.paperList() exception: " + e.toString());
        }
    }
    /************************************************* End of Paper List **********************************************/


    public Result loadLDA() {

        List<Paper> papers = new ArrayList<>();

        try {
            //new LDA().train();

            return ok("1");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.loadLDA() exception: " + e.toString());
            return internalServerError("PaperController.loadLDA() exception: " + e.toString());
        }
    }
    /*********************************************** Get Paper By Id***************************************************/
    /**
     * Get a paper detail by the paper id
     *
     * @param paperId paper Id
     * @return ok if the paper is found; badRequest if the paper is not found
     */
    public Result getPaperById(Long paperId) {
        if (paperId == null) {
            return Common.badRequestWrapper("paperId is null or empty.");
        }

        if (paperId == 0) return ok(Json.toJson(null));  // paperId=0 means OpenNEX paper, not stored in DB

        try {
            Paper paper = Paper.find.query().where().eq("id", paperId).findOne();
            List<AuthorPaper> authorsPapers = AuthorPaper.find.query().where().eq("paperId", paperId).
                    findList();
            List<Author> authors = new ArrayList();
            for (AuthorPaper a_p : authorsPapers) {
                authors.add(Author.find.query().where().eq("id", a_p.getAuthorId()).findOne());
            }
            paper.setAuthors(authors);
            JsonNode jsonNode = Json.toJson(paper);
            String result = jsonNode.toString();

            return ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.getPaperById() exception : " + e.toString());
            return internalServerError("Internal Server Error PaperController.getPaperById() exception: " +
                    e.toString());
        }
    }

    /******************************************** End of Get Paper By Id***********************************************/


    /****************************************** Get Author for Paper By Id*********************************************/

    /**
     * This method intends to get team members by paper id.
     *
     * @param paperId paper Id(from paper info table)
     * @return the team member when given valid Id. Otherwise, badRequest.
     */
    public Result getAuthorByPaperId(Long paperId) {
        if (paperId == null) {
            return Common.badRequestWrapper("paperId is null or empty.");
        }
        try {
            Paper paper = Paper.find.byId(paperId);

            List<Author> teamMembers = paper.getAuthors();
            return ok(Common.objectList2JsonArray(teamMembers));
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.getTeamMembersByPaperId() exception: " + e.toString());
            return internalServerError("PaperController.getTeamMembersByPaperId() exception: " + e.toString());
        }
    }

    /************************************** End of Get Author for Paper By Id******************************************/

    /************************************************* Delete Author***************************************************/

    /**
     * This method intends to delete a team member by member id.
     *
     * @param memberId: team member id
     * @return status
     */
    public Result deleteAuthor(Long memberId) {
        if (memberId == null) {
            return Common.badRequestWrapper("memberId is null.");
        }
        try {
            Author tm = Author.find.byId(memberId);
            if (tm != null) {
                tm.delete();
                return ok("Author deleted successfully for member id:" + memberId);
            } else
                return Common.badRequestWrapper("memberId cannot be found thus not deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Team member cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete team member for id: " + memberId);
        }
    }

    /********************************************* End od Delete Author************************************************/


    /********************************************* End of Delete Paper*************************************************/
    /**
     * Get Paper Author List
     *
     */
    public Result paperAuthorList() {

       try {
            int numofRecordsToLoad = Author.find.query().findList().size()+1;;
            List<AuthorPaper> authorsPapers = AuthorPaper.find.query().findList();

           int count = 0;
            Map<Long, List<Author>> mapA = new HashMap<>();

            for (AuthorPaper a_p : authorsPapers) {
                if(count++<numofRecordsToLoad) {
                    List<Author> authors = new ArrayList<>();
                    if (mapA.containsKey(a_p.getPaperId())) {
                        authors = mapA.get(a_p.getPaperId());
                        authors.add(Author.find.query().where().eq("id", a_p.getAuthorId()).findOne());
                        mapA.put(a_p.getPaperId(), authors);
                    } else {
                        authors.add(Author.find.query().where().eq("id", a_p.getAuthorId()).findOne());
                        mapA.put(a_p.getPaperId(), authors);
                    }
                }
            }
            count=0;
            Map<Long, List<Paper>> mapP = new HashMap<>();

            for (AuthorPaper a_p : authorsPapers) {
                if(count++<numofRecordsToLoad) {
                    List<Paper> papers = new ArrayList<>();
                    if (mapP.containsKey(a_p.getAuthorId())) {
                        papers = mapP.get(a_p.getAuthorId());
                        papers.add(Paper.find.query().where().eq("id", a_p.getPaperId()).findOne());
                        mapP.put(a_p.getAuthorId(), papers);
                    } else {
                        papers.add(Paper.find.query().where().eq("id", a_p.getPaperId()).findOne());
                        mapP.put(a_p.getAuthorId(), papers);
                    }
                }
            }
            int[][] matrix = new int[numofRecordsToLoad][numofRecordsToLoad];
            int[][] transposeMatrix = new int[numofRecordsToLoad][numofRecordsToLoad];
            int[][] relationMatrix = new int[numofRecordsToLoad][numofRecordsToLoad];

            int i1 = 0;
            int j1 = 0;
            for (long authorId : mapP.keySet()) {
                if (i1++ < numofRecordsToLoad) {
                    for (Paper p : mapP.get(authorId)) {
                        if (j1++ < numofRecordsToLoad) {
                            int l = new Long(p.getId()).intValue();
                            matrix[Integer.parseInt(String.valueOf(authorId))][l] = 1;
                        }
                        j1 = 0;
                    }
                }
            }
            i1 = 0;
            j1 = 0;
            for (int i = 0; i < mapP.size(); i++) {
                if (i1++ < numofRecordsToLoad) {
                    for (int j = 0; j < mapP.size(); j++) {
                        if (j1++ < numofRecordsToLoad) {
                            transposeMatrix[i][j] = matrix[j][i];
                        }
                        j1 = 0;
                    }
                }
            }

            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < transposeMatrix.length; j++) {
                    relationMatrix[i][j] = matrix[i][j] * transposeMatrix[j][i];
                    System.out.print(relationMatrix[i][j]);

                }
                System.out.println(" ");
            }
            String result = paperService.constructPaperAuthorRelation(relationMatrix,mapA);
            return ok(result);
        } catch (Exception e) {
           e.printStackTrace();
            Logger.debug("PaperController.authorRelation() exception: " + e.toString());
            return internalServerError("PaperController.authorRelation() exception: " + e.toString());
        }

    }

    /**
     * Get Author Author Relation
     */
    public Result authorRelation() {

        try {
            int numofRecordsToLoad = Author.find.query().findList().size()+1;
            List<AuthorPaper> authorsPapers = AuthorPaper.find.query().findList();
            int count = 0;
            Map<Long, List<Paper>> mapP = new HashMap<>();

            for (AuthorPaper a_p : authorsPapers) {
                if(count++<numofRecordsToLoad) {
                    List<Paper> papers = new ArrayList<>();
                    if (mapP.containsKey(a_p.getAuthorId())) {
                        papers = mapP.get(a_p.getAuthorId());
                        papers.add(Paper.find.query().where().eq("id", a_p.getPaperId()).findOne());
                        mapP.put(a_p.getAuthorId(), papers);
                    } else {
                        papers.add(Paper.find.query().where().eq("id", a_p.getPaperId()).findOne());
                        mapP.put(a_p.getAuthorId(), papers);
                    }
                }
            }

            int[][] matrix = new int[numofRecordsToLoad][numofRecordsToLoad];
            int[][] transposeMatrix = new int[numofRecordsToLoad][numofRecordsToLoad];
            int[][] relationMatrix = new int[numofRecordsToLoad][numofRecordsToLoad];

            int i1 = 0;
            int j1 = 0;
            for (long authorId : mapP.keySet()) {
                if (i1++ < numofRecordsToLoad) {
                    for (Paper p : mapP.get(authorId)) {
                        if (j1++ < numofRecordsToLoad) {
                            int l = new Long(p.getId()).intValue();
                            matrix[Integer.parseInt(String.valueOf(authorId))][l] = 1;
                        }
                        j1 = 0;
                    }
                }
            }
            i1 = 0;
            j1 = 0;
            for (int i = 0; i < mapP.size(); i++) {
                if (i1++ < numofRecordsToLoad) {
                    for (int j = 0; j < mapP.size(); j++) {
                        if (j1++ < numofRecordsToLoad) {
                            transposeMatrix[i][j] = matrix[j][i];
                        }
                        j1 = 0;
                    }
                }
            }

            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < transposeMatrix.length; j++) {
                    relationMatrix[i][j] = matrix[i][j] * transposeMatrix[j][i];
                    System.out.print(relationMatrix[i][j]);

                }
                System.out.println(" ");
            }
            String result = paperService.constructAuthorRelation(relationMatrix);
            return ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.authorRelation() exception: " + e.toString());
            return internalServerError("PaperController.authorRelation() exception: " + e.toString());
        }

    }
    /**
     * Store DBLP file data
     *
     */
    public Result storeDBLP() throws IOException {

        JsonNode json = request().body().asJson();
        String xml = json.get("data").asText();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            SAXParserHandler parserhandler = new SAXParserHandler();
            saxParser.parse(new InputSource(new StringReader(xml)), parserhandler);
            List<Paper> papers = SAXParserHandler.records;
            File file = new File("LDAAbstract.csv");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            int count=0;int idx=0;
            for (Paper paper : papers) {
                    List<Author> authors = new ArrayList<>();
                    authors = paper.getAuthors();
                    paper.setAuthors(new ArrayList<>());
                    if(paper.getTitle()!=null){
                        String title = paper.getTitle().replaceAll("=","");
                    paper.setAbstractText(paperService.getAbstract(title));

                    if (paper.getAbstractText() != null && paper.getAbstractText().length() > 10) {
                        if (count > 0)
                            bufferedWriter.write("\n");
                        bufferedWriter.write(++count + ",");
                        if(idx==10)
                            idx=0;
                        bufferedWriter.write(paper.getBookTitle()+idx++ + ",");
                        bufferedWriter.write(paper.getTitle() + ",");
                        String text =paper.getAbstractText().replaceAll("\"","");
                        text=text.replaceAll("\\.","");
                        if(text.length()>1000){
                            text=text.substring(0,999);
                                                }
                        bufferedWriter.write("\""+text+".\"");

                    }
                    paper.save();
                    Paper paperNew = new Paper(paper.getId());


                    for (Author author : authors) {
                        Author authorFound = null;
                        authorFound = Author.find.query().where().eq("author_name",
                                author.getAuthorName()).findOne();
                        if (authorFound == null && author.getAuthorName()!=null) {
                            authorFound = new Author(author.getAuthorName(), "");

                            List<Paper> paperList = new ArrayList<Paper>();
                            paperList.add(paper);
                            authorFound.setPapersByAuthor(paperList);
                            authorFound.save();
                        } else {

                            List<Paper> paperList = authorFound.getPapersByAuthor();
                            if (paperList == null) {
                                paperList = new ArrayList<Paper>();
                                paperList.add(paperNew);

                            } else {
                                if (!paperList.contains(paperNew))
                                    paperList.add(paperNew);
                            }
                            authorFound.setPapersByAuthor(paperList);
                            authorFound.update();
                        }
                    }

                }
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.storeDBLP() exception: " + e.toString());
            return internalServerError("PaperController.storeDBLP() exception: " + e.toString());
        }

        return ok("");
    }
    public Result storeDBLPSchema() throws IOException {

        JsonNode json = request().body().asJson();
        String xml = json.get("data").asText();
        String fileName = json.get("name").asText();
        File file = new File(fileName);
        BufferedWriter buffer=new BufferedWriter(new FileWriter(file));
        buffer.write(xml);
        buffer.close();
        return ok("");
    }
    public Result ldaTopicDistribution() {
        String result="";
        try {
            result=paperService.getLDATopicDistibution();

        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("PaperController.storeDBLP() exception: " + e.toString());
            return internalServerError("PaperController.storeDBLP() exception: " + e.toString());
        }
        return ok(result);
    }

}
