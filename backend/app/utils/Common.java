package utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import models.User;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import com.typesafe.config.Config;
public class Common {

	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";
	public static final String DATASET_DATE_PATTERN = "yyyyMM";

	public static Result badRequestWrapper(String s)
	{
		Map<String, String> map = new HashMap<>();
		map.put("error", s);
		String error = Json.toJson(map).toString();
		return Results.ok(error);
	}

	public static String genToken() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Creates a back-end url that can be passed on, using the path.
	 *
	 * @param config the configuration settings.
	 * @param path the relative path to generate the full path from.
	 * @return the absolute back-end path of the REST API to be called.
	 */
	public static String getBackendUrl(Config config, String path) {
		// Get the data from the application configuration.
		String protocol = config.getString("system.backend.protocol");
		String host = config.getString("system.backend.host");
		String port = config.getString("system.backend.port");
		// Build a proper URL
		StringBuilder url = new StringBuilder(protocol + "://");
		url.append(host);
		if (port != null && port != "") {
			url.append(":" + port);
		}
		url.append(path);
		return url.toString();
	}
    /**
     * Creates a front-end url that can be passed on, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute front-end path of the REST API to be called.
     */
    public static String getFrontendUrl(Config config, String path) {
        // Get the data from the application configuration.
        String protocol = config.getString("system.frontend.protocol");
        String host = config.getString("system.frontend.host");
        String port = config.getString("system.frontend.port");
        // Build a proper url.
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append(host);
        if (port != null && port != "") {
            url.append(":" + port);
        }
        url.append(path);
        return url.toString();
    }

	public static Result NotFoundWrapper(String s, String content)
	{
		Map<String, String> map = new HashMap<>();
		map.put("error", s + " not found with id: " + content);
		String error = Json.toJson(map).toString();
		return Results.ok(error);
	}

	/**
	 * This method adds models from initial list to the final list based on the offset and the limit
	 * Basically it is copying from the offset index to the end index (offset+limit) of the initial list to the final
	 * list while considering null values
	 *
	 * @param offset start index for copying
	 * @param limit end index for copying
	 * @param initialList initial list of data
	 * @return paginatedList
	 */
	public static <T> List<T> paginate(int offset, int limit, List<T> initialList) {
		List<T> finalList = new ArrayList<>();
		// todo
		for (int i = offset; i < (offset+limit); i++) {
			if (i < initialList.size()) {
				T item = initialList.get(i);
				if (item != null) {
					finalList.add(item);
				}
			}else {
				break;
			}
		}
		return finalList;
	}

	public static Result NotFoundWrapper(String s, JsonNode content)
	{
		return NotFoundWrapper(s, content.asText());
	}

	/**
	 * Turn a list into json array
	 *
	 * @param objectList list of objects
	 * @return json array of serialized notebooks
	 */
	public static <T> ArrayNode objectList2JsonArray(List<T> objectList) {
		ArrayNode arrayNode = Json.newArray();
		for (T object : objectList) {
			ObjectNode objectNode = (ObjectNode) Json.toJson(object);
			arrayNode.add(objectNode);
		}
		return arrayNode;
	}

	/**
	 * Get the sort type
	 * @param sort shows based on which column we want to sort the data
	 * @param defaultSortCriteria this is the default sort criteria used if no sort type is identified
	 * @return sort type.
	 */
	public static String getSortCriteria(Optional<String> sort, String defaultSortCriteria) {
		// by default, the sort criteria is sort by service register timestamp, in descending order
		String sortCriteria = defaultSortCriteria;
		// Check the sort order. If the sort order is empty, set it to the default which is publish_time_stamp.
		if (sort.isPresent() && !sort.get().equals("")) {
			// If it is popularity, for now check notebook's followers.
			sortCriteria = sort.get();
		}
		return sortCriteria;
	}

	/**
	 * Upload file to AWS S3
	 * TODO: Should use constants, also change method name
	 * @param config
	 * @param table table name used to build up key name
	 * @param type "Image" or "Pdf" or "Notebook"
	 * @param id id of the object
	 * @param request
	 * @return S3 file keyName.
	 */
	public static String uploadFile(Config config, String table, String type, long id, Http.Request request){
		File file = request.body().asRaw().asFile();

		String keyName = table + "/" + type + "/" + id;
		if (type.equals("Pdf")) {
			keyName += ".pdf";
		} else if (type.equals("Notebook")) {
			keyName += ".ipynb";
		}
		String bucket = config.getString("system.aws.bucket");
		final AmazonS3 s3 = buildS3Client(config);
		s3.putObject(new PutObjectRequest(bucket, keyName, file).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
		return keyName;
	}

	public static String uploadFileOnServer(String className, String type, long id, Http.Request request) throws IOException {
		String fileSeparator = File.separator;
		String filePath = System.getProperty("user.dir") + fileSeparator + "data" + fileSeparator + className + fileSeparator + type + fileSeparator;
		File fileDirs= new File(filePath);
		if(!fileDirs.exists()){
			fileDirs.mkdirs();
		}
		if(type == "Image"){
			filePath = filePath + id+ ".jpg";

		}else if (type == "Pdf"){
			filePath = filePath + id+ ".pdf";
		}
		try{

			File file = request.body().asRaw().asFile();

			File tempFile = new File(filePath);

			file.renameTo(tempFile);


		}catch(Exception e){
			e.printStackTrace();
		}
		return filePath;
	}

	/**
	 * get file's presigned url with limit time without authtication
	 * @param config
	 * @param keyName obejct key name
	 * @param expire seconds to expire
	 */
	public static String getSignedURL(Config config, String keyName, long expire){
		String bucket = config.getString("system.aws.bucket");
		try {
			final AmazonS3 s3 = buildS3Client(config);
			java.util.Date expiration = new java.util.Date();
			long expTimemillis = Instant.now().toEpochMilli();
			expTimemillis += 1000 * expire;
			expiration.setTime(expTimemillis);
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, keyName)
					.withMethod(HttpMethod.GET)
					.withExpiration(expiration);
			URL signedurl = s3.generatePresignedUrl(generatePresignedUrlRequest);
			return signedurl.toString();
			} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
			} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
			}
			return null;
		}

	/**
	 * Build AWS S3 Client
	 * @param config
	 * @return S3 Client.
	 */
	public static AmazonS3 buildS3Client(Config config){
		String awsAccessKey = config.getString("system.aws.access-key");
		String awsSecretAccesskey = config.getString("system.aws.secret-access-key");
		String awsRegion = config.getString("system.aws.region");
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretAccesskey);
		return  AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(awsRegion)
				.build();
	}

	/**
	 * Delete file from AWS S3
	 * @param config
	 * @param table table name used to build up key name, with file extension.
	 * @param type "Image" or "Pdf" or "Notebook"
	 * @param id id of the object
	 *
	 * @relatedTo deleteFileFromS3(Config config, String keyName) where keyName is already prepared)
	 */
	public static void deleteFileFromS3(Config config, String table, String type, long id) throws Exception {
		Logger.info("Common.deleteFileFromS3 from S3: " + table + " " + type);
		try {
			String keyName = table + type + "-" + id;
			if (type.equals("Pdf")) {
				keyName += ".pdf";
			} else if (type.equals("Notebook")) {
				keyName += ".ipynb";
			}
			deleteFileFromS3(config, keyName);
		} catch (Exception e) {
			Logger.debug("Failed to delete file type: " + type + " for " + table +": " + e.toString());
			throw e;
		}
	}

	/**
	 * Delete file from AWS S3, not considering file extension.
	 * @param config
	 * @param keyName
	 */
	public static void deleteFileFromS3(Config config, String keyName) throws Exception {
		try {
			String bucket = config.getString("system.aws.bucket");
			final AmazonS3 s3 = buildS3Client(config);
			s3.deleteObject(new DeleteObjectRequest(bucket, keyName));
		} catch (Exception e) {
			Logger.debug("Failed to delete file from S3: " + e.toString());
			throw e;
		}
	}


	/**
	 * This method intends to return a JsonNode from a file.
	 * @param file
	 * @return
	 */
	public static JsonNode readJsonFromFile(File file){
		if(file == null){
			System.out.println("null file");
			return null;
		}
		StringBuilder contentBuilder = new StringBuilder();
		try{
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String sCurrentLine;
			while((sCurrentLine = br.readLine()) != null){
				contentBuilder.append(sCurrentLine).append("\n");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		ObjectMapper mapper = new ObjectMapper();

		try {

			JsonNode datasetJsonNode = mapper.readTree(contentBuilder.toString());
			return datasetJsonNode;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convert a JSON string to pretty print version
	 *
	 * @param jsonString
	 * @return
	 */
	public static String toPrettyFormat(String jsonString) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonString).getAsJsonObject();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String prettyJson = gson.toJson(json);

		return prettyJson;
	}

	public static Set<Constants.USER_TYPE> getUserTypes(User user) {
		return getUserTypes(user.getUserType());
	}

	public static Set<Constants.USER_TYPE> getUserTypes(int userType) {
		Set<Constants.USER_TYPE> userTypes = new HashSet<>();
		for (Constants.USER_TYPE utp : Constants.USER_TYPE.values()) {
			if ((userType & utp.value()) == utp.value()) userTypes.add(utp);
		}
		return userTypes;
	}
}
