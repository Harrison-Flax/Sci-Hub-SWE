package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.ExpressionList;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.AuthorService;
import services.UserService;
import utils.Common;

import java.util.*;
import java.util.stream.Collectors;

import static utils.Constants.ACTIVE;

public class AuthorController extends Controller {

    public static final String AUTHOR_DEFAULT_SORT_CRITERIA = "authorName";
    @Inject
    Config config;

    private final AuthorService authorService;
    private final UserService userService;

    @Inject
    public AuthorController(UserService userService,AuthorService authorService) {
        this.authorService = authorService;
        this.userService = userService;
    }


    /*********************************************** Add Author *******************************************************/
    /**
     * Registers a new author provided the author data in the request body.
     *
     * @return the author id of the author created.
     */
    public Result addAuthor() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                return Common.badRequestWrapper("Author not created, expecting Json data");
            }

            final Author author = Json.fromJson(json, Author.class);
            author.setAuthorName(author.getFirstName() + " " + author.getLastName());

            if ((Author.find.query().where().eq("email", author.getEmail()).findList()).size() != 0) {
                return Common.badRequestWrapper("Email has been used");
            }

            author.save();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", author.getId());
            return ok(node);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.addAuthor() exception " + e.toString());
            return notFound("Author not added");
        }
    }
    /*********************************************** End of Add Author ************************************************/


    /*********************************************** Update Author ****************************************************/
    /**
     * This method receives updated information about author and saves the changes
     *
     * @return ok or bad request
     */
    public Result updateAuthor() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                return Common.badRequestWrapper("Cannot update author, expecting Json data");
            }

            Author updatedAuthor = Json.fromJson(json, Author.class);
            updatedAuthor.setAuthorName(Author.createAuthorName(updatedAuthor.getFirstName(),
                    updatedAuthor.getMiddleInitial(), updatedAuthor.getLastName()));

            updatedAuthor.update();

            return ok("updated");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.updateAuthor exception: " + e.toString());
            return internalServerError("User could not be updated.");
        }
    }

    public Result updateUserAdmin() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                return Common.badRequestWrapper("Cannot update author, expecting Json data");
            }

            Author updatedAuthor = Json.fromJson(json, Author.class);
            updatedAuthor.setAuthorName(Author.createAuthorName(updatedAuthor.getFirstName(),
                    updatedAuthor.getMiddleInitial(), updatedAuthor.getLastName()));

            updatedAuthor.update();

            return ok("updated");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.updateAuthor exception: " + e.toString());
            return internalServerError("User could not be updated.");
        }
    }
    /*********************************************** End of Update Author *********************************************/

    /*********************************************** Author Detail ****************************************************/
    /**
     * This method intends to returns a author's information from a author id.
     *
     * @param authorId author authorId
     * @return author
     */
    public Result authorDetail(Long authorId) {
        if (authorId == null) {
            Logger.debug("Author authorId is null or empty for AuthorController.authorDetail");
            return Common.badRequestWrapper("Author is not valid");
        }

        try {
//            Author author = Author.find.query().where().eq("id", authorId).findOne();
//            ResearcherInfo researcher = ResearcherInfo.find.query().where().eq("user_id", authorId).findOne();
            User researcher = User.find.query().where().eq("id", authorId).eq("user_type % 2", 1).findOne();
            if (researcher == null) {
                Logger.info("Author not found with authorId: " + authorId);
                return notFound("Author not found with authorId: " + authorId);
            }
            ResearcherInfo researcherInfo = ResearcherInfo.find.query().where().eq("user_id", authorId).findOne();
            researcher.setResearcherInfo(researcherInfo);
            JsonNode jsonNode = Json.toJson(researcher);
            String result = jsonNode.toString();
            Logger.debug(result);
            return ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.authorDetail exception: " + e.toString());
            return notFound("Author was not found.");
        }
    }

    public Result userDetailAdmin(Long userId) {
        if (userId == null) {
            Logger.debug("User userId is null or empty for UserController.userDetail");
            return Common.badRequestWrapper("User is not valid");
        }

        try {
//            User user = User.find.query().where().eq("is_active", ACTIVE).eq("id", userId).findOne();
            User user = User.find.query().where().eq("id", userId).findOne();

            if (user == null) {
                Logger.info("User not found with userId: " + userId);
                return notFound("User not found with userId: " + userId);
            }
            Logger.info("User found with userId: " + userId);
            Logger.info("User avatar before signing: " + user.getAvatar());

            String avatarKey = user.getAvatar();
            String s3Prefix = "https://ecopro-aws-bucket.s3.amazonaws.com/";
            String avatarURL = "";
            if (avatarKey == null || avatarKey.isEmpty()) {
                Logger.info("Avatar is null or empty, using default avatar if available.");
            } else {
                if (avatarKey.startsWith(s3Prefix)) {
                    avatarKey = avatarKey.substring(s3Prefix.length());
                }
                avatarURL = Common.getSignedURL(config, avatarKey, 60);
            }
            Logger.info("Signed avatar URL: " + avatarURL);

            user.setAvatar(avatarURL);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.valueToTree(user);
            String result = node.toString();
            return ok(result);
        } catch (Exception e) {
            Logger.debug("UserController.userDetail exception: " + e.toString());
            return notFound("User was not found.");
        }
    }
    /*********************************************** End of Author Detail *********************************************/

    /*********************************************** Author List ******************************************************/
    /**
     * Gets a list of all the authors based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param offset       shows the start index of the rows we want to receive
     * @param sortCriteria shows based on what column we want to sort the data
     * @return the list of authors.
     */
    public Result authorList(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
//        List<Author> authors = new ArrayList<>();
        List<User> researchers = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, AUTHOR_DEFAULT_SORT_CRITERIA);

        try {

//                authors = Author.find.query().orderBy(sortOrder).findList();
//            researchers = User.find.query().where().eq("user_type % 2", 1).orderBy(sortOrder).findList();
            List<ResearcherInfo> researcherInfos = ResearcherInfo.find.query().findList();
            for (ResearcherInfo researcherInfo : researcherInfos) {
                User user = researcherInfo.getUser();
                if (user != null
                        && user.getUserType() != null
                        && user.getUserType() % 2 == 1) {
                    user.setResearcherInfo(researcherInfo);
                    researchers.add(user);
                }
            }

            RESTResponse response = authorService.paginateResults(researchers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            return notFound("Author not found");
        }
    }

    public Result userListAdmin(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
//        List<Author> authors = new ArrayList<>();
        List<User> researchers = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, AUTHOR_DEFAULT_SORT_CRITERIA);

        try {

//                authors = Author.find.query().orderBy(sortOrder).findList();
//            researchers = User.find.query().where().eq("user_type % 2", 1).orderBy(sortOrder).findList();
            List<User> listUsers = new ArrayList<>();
            listUsers = User.find.query().findList();
            RESTResponse response = userService.paginateResults(listUsers, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            return notFound("Author not found");
        }
    }


    /****************************************** End of Author List ****************************************************/

    /****************************************** Author Search List ****************************************************/

    /**
     * Find author by multiple condition, including authorname, affiliation, email, mailing address, phone number,
     * research area, etc.
     *
     * @return authors that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchAuthorByCondition(Optional<Integer> pageLimit, Optional<Integer> offset,
                                          Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, AUTHOR_DEFAULT_SORT_CRITERIA);
        JsonNode json = request().body().asJson();
        List<User> users = new ArrayList<>();
        if (json == null) {
            Logger.debug("Author cannot be queried, expecting Json data");
            return badRequest("Author cannot be queried, expecting Json data");
        }
        try {
            System.out.println("***" + json);

            // 提取搜索条件；如果不存在，则设为空字符串
            String authorName = json.has("AuthorName") ? json.get("AuthorName").asText() : "";
            String email = json.has("Email") ? json.get("Email").asText() : "";
            String researchFields = json.has("ResearchArea") ? json.get("ResearchArea").asText() : "";

            // 判断哪些条件被提供（非空字符串）
            boolean emailProvided = email != null && !email.trim().isEmpty();
            boolean nameProvided = authorName != null && !authorName.trim().isEmpty();
            boolean researchProvided = researchFields != null && !researchFields.trim().isEmpty();
            boolean anyProvided = emailProvided || nameProvided || researchProvided;

            // 根据 researchFields 查询，如果提供了则用 icontains，否则查询所有记录
            // 注意：这里即使没有提供 researchFields，也会查询所有记录，然后再按其它条件进行匹配
            List<ResearcherInfo> researcherInfos;
            if (researchProvided) {
                researcherInfos = ResearcherInfo.find.query().where()
                        .icontains("research_fields", researchFields)
                        .findList();
            } else {
                researcherInfos = ResearcherInfo.find.query().findList();
            }

            // 遍历查询到的记录，根据各个条件进行“或”匹配
            // 只有当任一提供的条件（Email、AuthorName、ResearchArea）匹配时才算匹配
            // 如果所有条件均未提供，则 anyProvided 为 false，此时不匹配任何记录
            for (ResearcherInfo researcherInfo : researcherInfos) {
                User user = researcherInfo.getUser();
                boolean match = false;

                if (emailProvided && user.getEmail() != null &&
                        user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                    match = true;
                }
                if (nameProvided && user.getUserName() != null &&
                        user.getUserName().toLowerCase().contains(authorName.toLowerCase())) {
                    match = true;
                }
                if (researchProvided && researcherInfo.getResearchFields() != null &&
                        researcherInfo.getResearchFields().toLowerCase().contains(researchFields.toLowerCase())) {
                    match = true;
                }

                // 如果所有条件都为空，则 anyProvided 为 false，此时 match 仍为 false
                if (match) {
                    user.setResearcherInfo(researcherInfo);
                    users.add(user);
                }
            }

            System.out.println("***" + users.size());
            RESTResponse response = authorService.paginateResults(users, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Author cannot be queried, query is corrupted");
            return badRequest("Author cannot be queried, query is corrupted");
        }
    }
    /*************************************** End of Search Author List ************************************************/


    /******************************************* Sort Author List *****************************************************/


    /**
     * Sort the given list of authors
     *
     * @param authors      List of authors to be sorted
     * @param sortCriteria sort criteria
     * @return sorted list of authors.
     */
    public static void sortAuthors(List<Author> authors, String sortCriteria) {
        if (sortCriteria.equals("author_name")) {
            Comparator<Author> com = new Comparator<Author>() {
                @Override
                public int compare(Author o1, Author o2) {
                    String name1 = o1.getAuthorName();
                    String name2 = o2.getAuthorName();
                    return name1.toLowerCase().compareTo(name2.toLowerCase());
                }
            };
            Collections.sort(authors, com);
        } else {
            Comparator<Author> com = new Comparator<Author>() {
                @Override
                public int compare(Author o1, Author o2) {
                    return new Long(o1.getId()).compareTo(new Long(o2.getId()));
                }
            };
            Collections.sort(authors, com);
        }
    }


    /**************************************** End of Sort Author list *************************************************/


    /**
     * Search author by authorname
     *
     * @param display_name
     * @return the target author
     */
    public Result authorSearch(String display_name) {
        if (display_name == null) {
            System.out.println("Display name is null or empty!");
            return Common.badRequestWrapper("Display name is null or empty!");
        }
        List<Author> authors = Author.find.query().where().like("author_name", "%" + display_name +
                "%").findList();
        if (authors == null || authors.size() == 0) {
            return notFound("Author not found with with display name: " + display_name);
        }
        String result = new String();
        JsonNode jsonNode = Json.toJson(authors);
        result = jsonNode.toString();

        return ok(result);
    }

    public Result topAuthors() {
        return topResearchers();
    }

    public Result topAuthorsOld() {
        List<Author> authors = new ArrayList<>();

        try {
            List<AuthorPaper> authorsPapers = AuthorPaper.find.query().findList();
            Map<Long,Integer> map = new HashMap<>();
            for(AuthorPaper a_p:authorsPapers){
                if(map.containsKey(a_p.getAuthorId())) {
                    int num_papers = map.get(a_p.getAuthorId()) + 1;
                    map.put(a_p.getAuthorId(),num_papers);
                }
                else {
                    map.put(a_p.getAuthorId(),1);
                }
            }
            map.entrySet().stream()
                    .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                    .forEach(k -> map.put(k.getKey(),k.getValue()));
            List<Author> topAuthors = new ArrayList<Author>();
            for(Long authorId:map.keySet()){
                topAuthors.add(Author.find.query().where().eq("id", authorId).findOne());
            }
            RESTResponse response = new RESTResponse();
            ArrayNode projectsNode =
                    Common.objectList2JsonArray(topAuthors);

            System.out.println(response.toString());
            return ok(projectsNode.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            return notFound("Author not found");
        }
    }

    public Result topResearchers() {
        try {
            List<ResearcherInfo> researcherInfos = ResearcherInfo.find.query().findList();
            List<User> users = new ArrayList<>();
            for (ResearcherInfo researcherInfo : researcherInfos) {
                User user = researcherInfo.getUser();
                user.setResearcherInfo(researcherInfo);
                users.add(user);
            }

            RESTResponse response = new RESTResponse();
            ArrayNode projectsNode =
                    Common.objectList2JsonArray(users);

            System.out.println(response.toString());
            return ok(projectsNode.toString());
        } catch (Exception e) {
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            return notFound("Author not found");
        }
    }
}