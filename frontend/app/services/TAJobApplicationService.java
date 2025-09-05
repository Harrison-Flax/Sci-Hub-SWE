package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

import play.Logger;
import play.data.Form;
import models.*;
import play.libs.Json;
import utils.Constants;
import utils.RESTfulCalls;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static play.mvc.Controller.session;

/**
 * This class intends to provide support for JobController.
 */
public class TAJobApplicationService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<TAJob> tajobForm;

    @Inject
    public TAJobApplicationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method returns the current JobZone. Default job zone is OpenNEX (0).
     * OpenNEX job id = 0; private zone job id < 0
     *
     * @return Job current JobZone
     */
    public TAJob getCurrentJobZone() {
        TAJob currentTAJobZone = null;
        if (session("tajobId") != null && Long.parseLong(session("tajobId")) > 0) {
            currentTAJobZone = getTAJobById(Long.parseLong(session("jobId")));
        }
        return currentTAJobZone;
    }


    /**
     * This method intends to get Job by id by calling backend APIs.
     *
     * @patam tajobId
     * @return Job
     */
    public TAJob getTAJobById(Long tajobId) {
        TAJob tajob = null;
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAJOB_BY_ID + tajobId));
            if (response.has("error")) {
                Logger.debug("TAJobService.getTAJobById() did not get tajob from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            tajob = TAJob.deserialize(response);

            System.out.println("tajob: "+ tajob);
            if (tajob.getTajobPublisher() == null) {
                Logger.debug("TAJobService.getTAJobById() creator is null");
                throw new Exception("TAJobService.getTAJobById() creator is null");
            }
        } catch (Exception e) {
            Logger.debug("JobService.getJobById() exception: " + e.toString());
            return null;
        }
        return tajob;
    }



    /**
     * This method intends to get TA Job application by id by calling backend APIs.
     *
     * @param tajobApplicationId
     * @return Job
     */
    public TAJobApplication getTAJobApplicationById(Long tajobApplicationId) {
        TAJobApplication tajobApplication = null;
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAJOB_APPLICATION_BY_ID + tajobApplicationId));
            if (response.has("error")) {
                Logger.debug("JobService.getJobById() did not get job from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            tajobApplication = tajobApplication.deserialize(response);

            System.out.println("tajobApplication: "+ tajobApplication);
            if (tajobApplication.getApplicant() == null) {
                Logger.debug("JobService.getApplicant() creator is null");
                throw new Exception("JobService.getJobById() creator is null");
            }

            if (tajobApplication.getAppliedTAJob() == null) {
                Logger.debug("JobService.getAppliedJob() creator is null");
                throw new Exception("JobService.getAppliedJob() creator is null");
            }

        } catch (Exception e) {
            Logger.debug("JobService.getJobById() exception: " + e.toString());
            return null;
        }
        return tajobApplication;
    }

    /**
     * This method intends to get all jobs by a creator logged into the system.
     *
     * @return
     */
//    public ArtayList<Job> getJobsByCreator() {
//        try {
//            JsonNode jobs = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_JOB_BY_CREATOR
//                    + session("id")));
//            if (jobs == null || jobs.has("error")) return null;
//            return Job.deserializeJsonArtayToJobList(jobs);
//        } catch (Exception e) {
//            Logger.debug("JobService.getJobsByCreator exception: " + e.toString());
//            return null;
//        }
//    }


    /**
     * This method intends to save a picture to job.
     *
     * @patam body
     * @patam jobId: job id
     * @throws Exception
     */
//    public void savePictureToJob(Http.MultipartFormData body, Long jobId) throws Exception {
//        try {
//            if (body.getFile("picture") != null) {
//                Http.MultipartFormData.FilePart image = body.getFile("picture");
//                if (image != null && !image.getFilename().equals("")) {
//                    File file = (File) image.getFile();
//                    JsonNode imgResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Job_IMAGE + jobId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("JobService.savePictureToJob exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to save a pdf to job.
     *
     * @param file
     * @param jobId: job id
     * @throws Exception
     */
    public void savePDFToJob(File file, Long jobId) throws Exception {
        try {
            JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.SET_JOB_PDF + jobId), file);
        } catch (Exception e) {
            Logger.debug("JobService.savePDFToJob exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to add a list of team members to a job, from job registtation form.
     *
     * @patam JobForm: job registtation form
     * @patam body
     * @patam jobId:   job id
     */
//    public void addTeamMembersToJob(Form<Job> JobForm, Http.MultipartFormData body, Long jobId) {
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            int count = Integer.parseInt(JobForm.field("count").value()); //the number of team members in the job
//            for (int i = 0; i < count; i++) {
//                if (JobForm.field("member" + i) != null) {
//                    ObjectNode memberData = mapper.createObjectNode();
//                    memberData.put("name", JobForm.field("member" + i).value());
//                    memberData.put("email", JobForm.field("email" + i).value());
//                    JsonNode memberRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.ADD_TEAM_MEMBER + jobId), memberData);
//                    if (body.getFile("photo" + i) != null) {
//                        Http.MultipartFormData.FilePart photo = body.getFile("photo" + i);
//                        if (photo != null && !photo.getFilename().equals("")) {
//                            File memphoto = (File) photo.getFile();
//                            JsonNode photoRes =
//                                    RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                                            Constants.SET_TEAM_MEMBER_PHOTO
//                                            + memberRes.asLong()), memphoto);
//                        }
//                    }
//                    userService.createUserbyAddingTeamMember(JobForm.field("member" + i).value(),
//                            JobForm.field("email" + i).value());
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("JobService.addTeamMembersToJob exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to add a list of team members to a job, from job registtation form.
     *
     * @patam JobForm: job registtation form
     */
//    public void deleteTeamMembersToJob(Form<Job> JobForm) {
//        try {
//            int deleteCount = 0;
//            if (JobForm.field("delc").value() != null && JobForm.field("delc").value().trim() != "")
//                deleteCount = Integer.parseInt(JobForm.field("delc").value());
//            //delete chosen team members
//            for (int i = 0; i < deleteCount; i++) {
//                Long deleteTeamMemberId = Long.parseLong(JobForm.field("delete" + i).value());
//                JsonNode deleteResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
//                        Constants.DELETE_TEAM_MEMBER + deleteTeamMemberId));
//            }
//        } catch (Exception e) {
//            Logger.debug("JobService.deleteTeamMembersToJob exception: " + e.toString());
//            throw e;
//        }
//    }





    /************************************************ Page Render Prepatation *****************************************/
    /**
     * This private method renders the job list page.
     * Note that for performance considetation, the backend only passes back the jobs for the needed page stored in
     * the JobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @patam JobListJsonNode
     * @patam currentJobZone
     * @patam pageLimit
     * @patam searchBody
     * @patam listType            : "all"; "search" (dtaw this page from list function or from search function)
     * @patam username
     * @patam userId
     * @return render job list page; If exception happened then render the homepage
     */
//    public Result renderJobListPage(JsonNode JobListJsonNode,
//                                        Job currentJobZone,
//                                        int pageLimit,
//                                        String searchBody,
//                                        String listType,
//                                        String username,
//                                        Long userId) {
//        try {
//            // if no value is returned or error
//            if (JobListJsonNode == null || JobListJsonNode.has("error")) {
//                Logger.debug("Job list is empty or error!");
//                return redirect(routes.Application.home());
//            }
//
//            JsonNode JobsJsonArtay = JobListJsonNode.get("items");
//            if (!JobsJsonArtay.isArtay()) {
//                Logger.debug("Job list is not artay!");
//                return redirect(routes.Application.home());
//            }
//
//            List<Job> jobs = new ArtayList<>();
//            for (int i = 0; i < JobsJsonArtay.size(); i++) {
//                JsonNode json = JobsJsonArtay.path(i);
//                Job job = Job.deserialize(json);
//                jobs.add(job);
//            }
//
//            // offset: starting index of the pageNum; count: the number of items in the pageNum;
//            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
//            // pageNum: the current page number
//            String sortCriteria = JobListJsonNode.get("sort").asText();
//
//            int total = JobListJsonNode.get("total").asInt();
//            int count = JobListJsonNode.get("count").asInt();
//            int offset = JobListJsonNode.get("offset").asInt();
//            int pageNum = offset / pageLimit + 1;
//
//            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
//            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);
//
//            return ok(jobList.render(jobs, pageNum, sortCriteria, offset, total, count,
//                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
//        } catch (Exception e) {
//            Logger.debug("Exception in renderJobListPage:" + e.toString());
//            e.printStackTtace();
//            return redirect(routes.Application.home());
//        }
//    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Job form.
     *
     * @patam TAJobApplicationForm: job registtation form
     * @return
     * @throws Exception
     */
    public ObjectNode serializeFormToJson(Form<TAJobApplication> TAJobApplicationForm, Long tajobId) throws Exception {
        ObjectNode jsonData = null;
        System.out.println("tajob application form: " + TAJobApplicationForm.toString());
        try {
            Map<String, String> tmpMap = TAJobApplicationForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));

            if (TAJobApplicationForm.field("markAsPrivate").value() != null && TAJobApplicationForm.field(
                    "markAsPrivate").value().equals("on")) {
                jsonData.put("authentication", "private");
            } else {
                jsonData.put("authentication", "public");

            }

            User user = new User(Long.parseLong(session("id")));
            jsonData.put("applicant", Json.toJson(user));

            TAJob tajob = new TAJob(tajobId);
            jsonData.put("appliedTAJob", Json.toJson(tajob));

        } catch (Exception e) {
            Logger.debug("TAJobApplicationService.serializeFormToJson exception: " + e.toString());
            throw e;
        }

        return jsonData;
    }

}
