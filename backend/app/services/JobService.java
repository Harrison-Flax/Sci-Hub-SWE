package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Challenge;
import models.Job;
import models.JobApplication;
import models.rest.RESTResponse;
import utils.Common;

import java.util.List;
import java.util.Optional;


public class JobService {
    /**
     * This method intends to return a list of jobs based on optional offset and pageLimit and sort criteria
     *
     * @param jobs   all jobs
     * @param offset       shows the start index of the jobs rows we want to receive
     * @param pageLimit    shows the number of rows we want to receive
     * @param sortCriteria sort order
     * @return the list of jobs.
     */
    public RESTResponse paginateResults(List<Job> jobs, Optional<Integer> offset, Optional<Integer> pageLimit,
                                        String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = jobs.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= jobs.size())
            startIndex = pageLimit.get() * ((jobs.size() - 1) / pageLimit.get());
        List<Job> paginatedJobs = Common.paginate(startIndex, maxRows, jobs);
        response.setTotal(jobs.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }

    public RESTResponse paginateJobApplications(List<JobApplication> jobApplications, Optional<Integer> offset,
                                        Optional<Integer> pageLimit, String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = jobApplications.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= jobApplications.size())
            startIndex = pageLimit.get() * ((jobApplications.size() - 1) / pageLimit.get());
        List<JobApplication> paginatedJobs = Common.paginate(startIndex, maxRows, jobApplications);
        response.setTotal(jobApplications.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedJobs);
        response.setItems(jobsNode);
        return response;
    }

    public RESTResponse paginateJobApplications(String jobType, List<Object> applications, Optional<Integer> offset,
                                                Optional<Integer> pageLimit, String sortCriteria) {
        RESTResponse response = new RESTResponse();
        int maxRows = applications.size();
        if (pageLimit.isPresent()) {
            maxRows = pageLimit.get();
        }
        int startIndex = 0;
        if (offset.isPresent()) {
            startIndex = offset.get();
        }
        /******************************* paginate the list ************************************************************/
        if (startIndex >= applications.size())
            startIndex = pageLimit.get() * ((applications.size() - 1) / pageLimit.get());
        List<Object> paginatedApplications = Common.paginate(startIndex, maxRows, applications);
        response.setTotal(applications.size());
        response.setSort(sortCriteria);
        response.setOffset(startIndex);

        //return the entries as json array
        ArrayNode jobsNode = Common.objectList2JsonArray(paginatedApplications);
        response.setItems(jobsNode);
        return response;
    }
}
