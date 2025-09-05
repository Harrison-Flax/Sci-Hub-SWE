/**
 * @author xingyuchen
 * Created on Apr 21, 2016
 */
package utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.*;

public class Constants {
    static String configFilePath = System.getProperty("config.file");
    static Config config = configFilePath != null ? ConfigFactory.parseFile(new File(configFilePath)) : ConfigFactory.load();

    //AWS Setup
    public static final String AWS_ACCESS_KEY = "system.aws.access-key";
    public static final String AWS_SECRET_ACCESS_KEY = "system.aws.secret-access-key";
    public static final String AWS_REGION = "system.aws.region";
    public static final String AWS_BUCKET = "system.aws.bucket";

    public static final String AWS_BUCKET_NAME = config.getString("aws.s3.bucketName");

    public static final String AWS_FILE_NAME_PREFIX = config.getString("aws.fileNamePrefix");

    // server
    public static final String URL_HOST = "http://localhost";

    // port
    public static final String LOCAL_HOST_PORT = ":9068";
    public static final String CMU_BACKEND_PORT = ":9069";

    // active status
    public static final String ACTIVE = "True";

    // private project zone --> project id
    public static final int PRIVATE_PROJECT_ZONE_ID = -1;

    public static final int OPENNEX_PROJECT_ZONE_ID = 0;

    public static final String NASA_APPLICATION_APPROVED = "approved";
    public static final String NASA_APPLICATION_PENDING = "pending";
    public static final String NASA_APPLICATION_REJECTED = "rejected";

    // docker proposal status
    public static final int PROPOSAL_UNDER_REVIEW = 0;
    public static final int PROPOSAL_REJECTED = 1;
    public static final int PROPOSAL_ACCEPTED = 2;

    // API Call format
    public static final String FORMAT = "json";

    // add all parameter
    public static final String ADD_ALL_PARAMETERS = "/parameter/addParameter";

    public static final String GET_ALL_PUBLICATIONS = "/publication/getAllPublications/json";
    public static final String GET_PUBLICATION_PANEL = "/publication/getPublicationPanel/";

    // user
    public static final String USER_REGISTER = "/users/register";

    // INVITATION
    public static final String ACCEPT_INVITATION = "/representative/action/accept/";
    public static final String DECLINE_INVITATION = "/representative/action/decline/";
    public static final String UNAVAILABLE_INVITATION = "/representative/action/unavailable/";
    public static final String REPRESENTATIVE_EVALUATE = "/representative/evaluate/";

    // INVITATION
    public static final String ACCEPT_INVITATION_DOCKER = "/representative/action/accept/docker/";
    public static final String DECLINE_INVITATION_DOCKER = "/representative/action/decline/docker/";
    public static final String UNAVAILABLE_INVITATION_DOCKER = "/representative/action/unavailable/docker/";
    public static final String REPRESENTATIVE_EVALUATE_DOCKER = "/representative/evaluate/docker/";
	public static final long NASA_PENDING = 0;
	public static final long NASA_APPROVED = 1;
	public static final long NASA_REJECTED = 2;
	public static final String GET_ABSTRACT_API = "http://api.aminer.org/api/search/pub?query=";


    //HARDCODED EVENTS
    public static final String followedEvents = "New versions published;News published;";


    //GCMD_DATASET_PATH
    public static final String GCMD_DATASET_PATH = "../opennex-appstore-frontend/public/GCMDDataset/";

    // Forbidden Email format
    public static final Set<String> FORBIDDEN_EMAILS = new HashSet<>();

    public static enum USER_TYPE {
            GENERAL(0), RESEARCHER(1), SPONSOR(2), STUDENT(4);

        private int value = 0;
        private USER_TYPE(int value) {this.value = value;}

        public static USER_TYPE valueOf(int value) {
            switch(value) {
                case 0: return GENERAL;
                case 1: return RESEARCHER;
                case 2: return SPONSOR;
                case 4: return STUDENT;
                default: return null;
            }
        }

        public int value() {
            return this.value;
        }
    };

    static {
//		FORBIDDEN_EMAILS.add("@gmail.com");
//		FORBIDDEN_EMAILS.add("@hotmail.com");
//		FORBIDDEN_EMAILS.add("@outlook.com");
//		FORBIDDEN_EMAILS.add("@yahoo.com");
    }

}
