package utils;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.Logger;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import okhttp3.*;

public class RESTfulCalls {
    private static final WSClient WS = Play.current().injector().instanceOf(WSClient.class);

    private static final OkHttpClient client = new OkHttpClient();

    // These are for internal response
    public enum ResponseType {
        SUCCESS, GETERROR, SAVEERROR, DELETEERROR, RESOLVEERROR, TIMEOUT, CONVERSIONERROR, UNKNOWN
    }

    // These are for responding to users
    public enum UserResponseType {
        SUCCESS, GENERALERROR
    }

    /**
     * Performs a POST call with request body containing a file.
     *
     * @param apiString the API URL string
     * @param file      the file to add to the body
     * @return the response
     */
    public static JsonNode postAPIWithFile(String apiString, File file) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(2000000000, ChronoUnit.MICROS))
                .post(file);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)) {
                    try {
                        return response.asJson();
                    } catch (Exception e) {
                        //If response is in Json format, return as json, otherwise just plain success
                        return createResponse(ResponseType.SUCCESS);
                    }
                } else { // other response status from the server
                    System.out.println(response.getStatus());
                    return createResponse(ResponseType.SAVEERROR);
                }
            }
        });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Logger.debug("RESTfulCalls.postAPIWithFile: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    /**
     * Performs a GET operation expecting a file as a response.
     * <p>
     * param apiString the API URL string
     *
     * @return the response contains the file
     */
    public static File getAPIAsFile(String apiString) {
        Logger.info(apiString);
        if (WS == null) {
            // Logger.info("error");
            Logger.error("WS client is null. Cannot make API call.");
        }
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .get();
        final CompletionStage<File> bodyPromise = responsePromise.thenApplyAsync(new Function<WSResponse, File>() {
            public File apply(WSResponse response) {
                File image = null;
                FileOutputStream fos = null;
                try {
                    // Create a new file
                    image = File.createTempFile("app-", "");
                    fos = new FileOutputStream(image);
                    fos.write(response.asByteArray());
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return image;
            }
        });

        try {
            CompletableFuture<File> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode getAPI(String apiString) {
        Logger.info(apiString);
        if (WS == null) {
            Logger.info("error");
        }
        try {
            CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                    .setRequestTimeout(Duration.ofSeconds(30000))
                    .get();
            final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                    new Function<WSResponse, JsonNode>() {
                public JsonNode apply(WSResponse response) {
                    if (response.getStatus() == 200
                            || response.getStatus() == 201) {
                        return response.asJson();
                    } else { // no response from the server
                        Logger.info("" + response.getStatus());
                        return createResponse(ResponseType.GETERROR);
                    }
                }
            });

            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(30000, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }


    }

    public static JsonNode getAPIParameter(String apiString, String paraName, String para) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString).setQueryParameter(paraName, para)
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .get();
        final CompletionStage<JsonNode> bodyPromise = responsePromise
                .thenApplyAsync(new Function<WSResponse, JsonNode>() {
                    @Override
                    public JsonNode apply(WSResponse response) {
                        if (response.getStatus() == 200
                                || response.getStatus() == 201) {
                            return response.asJson();
                        } else { // no response from the server
                            Logger.info("" + response.getStatus());
                            return createResponse(ResponseType.GETERROR);
                        }
                    }
                });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    /**
     * Performs a POST call with request body containing a file
     * @param apiString the API URL String
     * @param file to add to the body
     * @return the response in JsonNode
     */
    public static JsonNode postWithFile(String apiString, File file) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(50000, ChronoUnit.MILLIS))
                .post(file);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)) {
                    try {
                        return response.asJson();
                    }
                    catch (Exception e){
                        //If response is in Json format, return as json, otherwise just plain success
                        return createResponse(ResponseType.SUCCESS);
                    }
                } else { // other response status from the server
                    return createResponse(ResponseType.SAVEERROR);
                }
            }
        });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    /**
     * This method intends to post an API call (either backend or front-end or MATA or StateTransfer etc), and generate
     * a response json.
     * @param apiString
     * @param jsonData
     * @return
     */
    public static JsonNode postAPI(String apiString, JsonNode jsonData) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(50000, ChronoUnit.MILLIS))
                .post(jsonData);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)) {
                    try {
                        return response.asJson();
                    } catch (Exception e) {
                        //If response is in Json format, return as json, otherwise just plain success
                        return createResponse(ResponseType.SUCCESS);
                    }
                } else { // other response status from the server
                    return createResponse(ResponseType.SAVEERROR);
                }
            }
        });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Logger.debug("RESTfulCalls.postAPI(): get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    /**
     * This method intends to post an API call (either backend or front-end or MATA or StateTransfer etc), and generate
     * a response json.
     * @param apiString
     * @param jsonData
     * @return
     */
    public static JsonNode postRestAPI(String apiString, JsonNode jsonData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonBody = mapper.writeValueAsString(jsonData);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            // Build the POST request
            Request request = new Request.Builder()
                    .url(apiString)
                    .post(body)
                    .build();
            // Execute the request and get the response
            try (Response response = client.newCall(request).execute()) {
                // Return the response body as a string
                return mapper.readTree(response.body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public static JsonNode putAPI(String apiString, JsonNode jsonData) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .put(jsonData);
        System.out.println("apiString" + apiString);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)
                        && !response.getBody().contains("not")) {
                    return createResponse(ResponseType.SUCCESS);
                } else { // other response status from the server
                    return createResponse(ResponseType.SAVEERROR);
                }
            }
        });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    public static JsonNode deleteAPI(String apiString) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString.replace(
                "+", "%20")).setContentType("text/html")
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .delete();
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 200 || response
                        .getStatus() == 201)
                        && !response.getBody().contains("not")) {
                    return createResponse(ResponseType.SUCCESS);
                } else { // no response from the server
                    return createResponse(ResponseType.DELETEERROR);
                }
            }
        });

        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    /**
     * Creates a back-end url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute back-end path of the REST API to be called.
     */
    public static String getBackendAPIUrl(Config config, String path) {
        // Get the data from the application configuration.
        String protocol = config.getString("system.backend.rest-protocol");
        String host = config.getString("system.backend.rest-host");
        String port = config.getString("system.backend.rest-port");

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
     * Creates a front-end url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute back-end path of the REST API to be called.
     */
    public static String getFrontendAPIUrl(Config config, String path) {
        // Get the data from the application configuration.
        String protocol = config.getString("system.frontend.rest-protocol");
        String host = config.getString("system.frontend.rest-host");
        String port = config.getString("system.frontend.rest-port");

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
     * Creates a MATA url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute back-end path of the REST API to be called.
     */
    public static String getMATAAPIUrl(Config config, String path) {
        String protocol = config.getString("system.mata.protocol");
        String host = config.getString("system.mata.host");
        String port = config.getString("system.mata.port");
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append(host);
        if (port != null && port != "") {
            url.append(":" + port);
        }
        url.append(path);
        return url.toString();
    }

    /**
     * Creates a StateTransfer design url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute back-end path of the REST API to be called.
     */
    public static String getStateTransferDesignUrl(Config config, String path) {
        String protocol = config.getString("system.statetransfer.protocol");
        String host = config.getString("system.statetransfer.host");
        String port = config.getString("system.statetransfer.port");
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append(host);
        if (port != null && port != "") {
            url.append(":" + port);
        }
        url.append(path);
        return url.toString();
    }

    /**
     * Creates a FASTTEXT url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path the relative path to generate the full path from.
     * @return the absolute back-end path of the REST API to be called.
     */
    public static String getFastTextAPIUrl(Config config, String path) {
        String protocol = config.getString("system.fastText.protocol");
        String host = config.getString("system.fastText.host");
        String port = config.getString("system.fastText.port");
        StringBuilder url = new StringBuilder(protocol + "://");
        url.append(host);
        if (port != null && port != "") {
            url.append(":" + port);
        }
        url.append(path);
        return url.toString();
    }

    /**
     * This method aims to create an internal response <key,value> pair in a JsonNode as response from a RESTfulCall,
     * based on the response type passed in.
     * @see public static JsonNode createResponse(String content)
     * @see public static JsonNode createUserResponse(UserResponseType type)
     */
    public static JsonNode createResponse(ResponseType type) {
        ObjectNode jsonData = Json.newObject();
        switch (type) {
            case SUCCESS:
                jsonData.put("success", "Success!");
                break;
            case GETERROR:
                jsonData.put("error", "Cannot get data from server");
                break;
            case SAVEERROR:
                jsonData.put("error", "Cannot be saved. The data must be invalid!");
                break;
            case DELETEERROR:
                jsonData.put("error", "Cannot be deleted on server");
                break;
            case RESOLVEERROR:
                jsonData.put("error", "Cannot be resolved on server");
                break;
            case TIMEOUT:
                jsonData.put("error", "No response/Timeout from server");
                break;
            case CONVERSIONERROR:
                jsonData.put("error", "Conversion error");
                break;
            default:
                jsonData.put("error", "Unknown errors");
                break;
        }
        return jsonData;
    }

    /**
     * This method aims to create a message response <key,value> pair in a JsonNode as response from a RESTfulCall,
     * using the content passed in.
     * @see public static JsonNode createResponse(ResponseType type)
     * @see public static JsonNode createUserResponse(UserResponseType type)
     */
    public static JsonNode createResponse(String content) {
        ObjectNode jsonData = Json.newObject();
        jsonData.put("message", content);
        return jsonData;
    }

    /**
     * This method aims to create a user response <key,value> pair in a JsonNode as response from a RESTfulCall,
     * based on the user response type passed in.
     * @see public static JsonNode createResponse(ResponseType type)
     * @see public static JsonNode createResponse(String content)
     */
    public static JsonNode createUserResponse(UserResponseType type) {
        ObjectNode jsonData = Json.newObject();
        switch (type) {
            case SUCCESS:
                jsonData.put("user_success", "Your action has been completed successfully!");
                break;
            case GENERALERROR:
                jsonData.put("user_general_error", "Your action did not go through.");
                break;
            }
        return jsonData;
    }

    public static JsonNode postAPIWithFileArray(String apiString,
                                                List<Http.MultipartFormData.Part<Source<ByteString, ?>>> files) {

        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(2000000000, ChronoUnit.MICROS))
                .post(Source.from(files));
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(
                new Function<WSResponse, JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)) {
                    try {
                        return response.asJson();
                    } catch (Exception e) {
                        //If response is in Json format, return as json, otherwise just plain success
                        return createResponse(ResponseType.SUCCESS);
                    }
                } else { // other response status from the server
                    return createResponse(ResponseType.SAVEERROR);
                }
            }
        });
        System.out.println("=======here...");
        try {
            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Logger.debug("RESTfulCalls.postAPIWithFile: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }


}
