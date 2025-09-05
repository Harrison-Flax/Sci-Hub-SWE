package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;

import com.typesafe.config.Config;

import static utils.Constants.VALID_EMAIL_ADDRESS_REGEX;

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
	// Display notification timestamp according to user's timezone
	public static String getTimeZone(String ip) {
		// Default time zone is PDT
		String res = "America/Los_Angeles";
		// Get info about this IP address
		JsonNode locationNode = RESTfulCalls.getAPI("http://ip-api.com/json/" + ip);
		if (locationNode != null && locationNode.get("status") != null &&
				locationNode.get("status").asText().equals("success") &&
				locationNode.get("timezone") != null && locationNode.get("timezone").size() > 0) {
			res = locationNode.get("timezone").toString();
		}

		return res;
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

	/**
	 * This method adds models from initial list to the final list based on start and end index
	 * Basically it is copying from the start index to the end index of the initial list to the final list while
	 * considering null values
	 * @param startIndex start index for copying
	 * @param endIndex end index for copying
	 * @param initialList initial list of data
	 * @param finalList the final list of data copied from initial list from start index to the end index
	 */
	public static <T> void paginate(int startIndex, int endIndex, List<T> initialList, List<T> finalList) {
		for (int i = startIndex; i <= endIndex; i++) {
			if (i < initialList.size()) {
				T object = initialList.get(i);
				if (object != null) {
					finalList.add(object);
				}
			}else {
				break;
			}
		}
	}

	/**
	 * This method gets the page limit and the list total number of items and the current page number and decides
	 * what should be the begining index for the pagination shown on the bottom of the page
	 * @param pageLimit pageLimit
	 * @param total the list total number of items
	 * @param page current page number
	 * @return begining index for the pagination shown on the bottom of the page
	 */
	public static int beginIndexForPagination(int pageLimit, int total, int page) {
		int beginIndexPagination = 1;
		if (page > 6) {
			if ((page+5) > ((total - 1) / pageLimit + 1)) {
				beginIndexPagination = ((total - 1) / pageLimit + 1)-9 > 0 ? ((total - 1) / pageLimit + 1)-9 : 1;
			}else{
				beginIndexPagination = page - 5;
			}
		}
		return beginIndexPagination;
	}

	/**
	 * This method gets the page limit and the list total number of items and the current page number and decides what
	 * should be the ending index for the pagination shown on the bottom of the page
	 * @param pageLimit pageLimit
	 * @param total the list total number of items
	 * @param page current page number
	 * @return ending index for the pagination shown on the bottom of the page
	 */
	public static int endIndexForPagination(int pageLimit, int total, int page) {
		int endIndexPagination = page + 4;
		if(page < 7){
			endIndexPagination = 10;
		}
		if (endIndexPagination > ((total - 1) / pageLimit + 1)) {
			endIndexPagination = ((total - 1) / pageLimit + 1);
		}
		return endIndexPagination;
	}

	public static boolean validate(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
		return matcher.find();
	}

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
	 * This method intends to return an ArrayList of objects from Json node.
	 * @param listJsonNode
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public static <T> ArrayList<T> deserializeJsonToList(JsonNode listJsonNode, Class<T> modelType) throws Exception {
		ArrayList<T> modelObjects = new ArrayList();
		int i = 0;
		for (JsonNode singleNode : listJsonNode) {
			modelObjects.add(Json.fromJson(singleNode, modelType));
		}
		return modelObjects;
	}

	/**
	 * This method receives the image in the body along with model id and uploads the image to AWS by calling the
	 * backend
	 *
	 * @param body body containing image
	 * @param modelId model Id
	 * @throws Exception
	 */
	public static void addImageForModel(Http.MultipartFormData body, Long modelId, Config config, String backendPath)
			throws Exception {
		Http.MultipartFormData.FilePart imageContent = body.getFile("image-file");
		try {
			if (imageContent != null && !imageContent.getFilename().isEmpty()) {
				File file = (File) imageContent.getFile();
				JsonNode addImageResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
						backendPath + modelId), file);
			}
		} catch (Exception e) {
			Logger.debug("Common.addImageForModel() exception: " + e.toString());
			throw e;
		}
	}

	/**
	 * This method returns a US-format date.
	 * @param time
	 * @return
	 */
	public static String getFormattedTime(Date time) {
		if (time == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate localDate = Instant.ofEpochMilli(time.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		return localDate.format(formatter);
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
