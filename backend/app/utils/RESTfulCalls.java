package utils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import lombok.NonNull;
import play.Logger;
import play.libs.Json;
import play.libs.ws.*;
import play.api.Play;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

public class RESTfulCalls {
    private static final WSClient WS;

    static {
        WS = Play.current().injector().instanceOf(WSClient.class);
    }

    public enum ResponseType {
        SUCCESS,
        GET_ERROR,
        SAVE_ERROR,
        DELETE_ERROR,
        RESOLVE_ERROR,
        TIMEOUT,
        CONVERSION_ERROR,
        UNKNOWN
    }

    public static String getToString(@NonNull final String url) {
        try {
            CompletionStage<WSResponse> responsePromise = WS.url(url)
                    //.setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                    .get();
            final CompletionStage<String> bodyPromise = responsePromise.thenApplyAsync((Function<WSResponse,
                    String>) response -> {
                if (response.getStatus() == 200
                        || response.getStatus() == 201) {
                    //							System.out.println("right");
                    return new String(response.asByteArray(), StandardCharsets.UTF_8);
                } else { // no response from the server
                    Logger.info("" + response.getStatus());
                    return createResponse(ResponseType.GET_ERROR).asText();
                }
            });

            CompletableFuture<String> future = bodyPromise.toCompletableFuture();
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT).asText();
        }
    }

    public static JsonNode getAPI(String apiString) {

        Logger.info(apiString);
        if (WS == null) {
            Logger.info("error");
        }
        try {

            CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                    .setRequestTimeout(Duration.ofSeconds(9000000))
                    .get();
            final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(new Function<WSResponse,
                    JsonNode>() {
                public JsonNode apply(WSResponse response) {
                    if (response.getStatus() == 200
                            || response.getStatus() == 201) {
//							System.out.println("right");
                        return response.asJson();
                    } else { // no response from the server
                        Logger.info("" + response.getStatus());
                        return createResponse(ResponseType.GET_ERROR);
                    }
                }
            });

            CompletableFuture<JsonNode> future = bodyPromise.toCompletableFuture();
            return future.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("get API exception: " + e);
            return createResponse(ResponseType.TIMEOUT);
        }
    }

    public static JsonNode getAPIParameter(@NonNull final String apiString,
                                           @NonNull final Map<String, String> queryParams) {
        final WSRequest request = WS.url(apiString);
        for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
            request.addQueryParameter(queryParam.getKey(), queryParam.getValue());
        }

        final CompletionStage<WSResponse> responsePromise = request
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .get();
        final CompletionStage<JsonNode> bodyPromise = responsePromise
                .thenApplyAsync(response -> {
                    if (response.getStatus() == 200
                            || response.getStatus() == 201) {
                        return response.asJson();
                    } else { // no response from the server
                        Logger.info("" + response.getStatus());
                        return createResponse(ResponseType.GET_ERROR);
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
                            return createResponse(ResponseType.GET_ERROR);
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

    public static JsonNode postAPI(String apiString, JsonNode jsonData) {
        System.out.println("apiString: " + apiString);
        System.out.println("jsonData: " + jsonData.toString());
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS))
                .post(jsonData);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(new Function<WSResponse,
                JsonNode>() {
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
                    return createResponse(ResponseType.SAVE_ERROR);
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

    public static JsonNode putAPI(String apiString, JsonNode jsonData) {
        CompletionStage<WSResponse> responsePromise = WS.url(apiString)
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .put(jsonData);
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(new Function<WSResponse,
                JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 201 || response
                        .getStatus() == 200)
                        && !response.getBody().contains("not")) {
                    return createResponse(ResponseType.SUCCESS);
                } else { // other response status from the server
                    return createResponse(ResponseType.SAVE_ERROR);
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
        CompletionStage<WSResponse> responsePromise = WS.url(apiString.replace("+", "%20")).
                setContentType("text/html")
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .delete();
        final CompletionStage<JsonNode> bodyPromise = responsePromise.thenApplyAsync(new Function<WSResponse,
                JsonNode>() {
            public JsonNode apply(WSResponse response) {
                if ((response.getStatus() == 200 || response
                        .getStatus() == 201)
                        && !response.getBody().contains("not")) {
                    return createResponse(ResponseType.SUCCESS);
                } else { // no response from the server
                    return createResponse(ResponseType.DELETE_ERROR);
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

    public static JsonNode createResponse(ResponseType type) {
        ObjectNode jsonData = Json.newObject();
        switch (type) {
            case SUCCESS:
                jsonData.put("success", "Success!");
                break;
            case GET_ERROR:
                jsonData.put("error", "Cannot get data from server");
                break;
            case SAVE_ERROR:
                jsonData.put("error", "Cannot be saved. The data must be invalid!");
                break;
            case DELETE_ERROR:
                jsonData.put("error", "Cannot be deleted on server");
                break;
            case RESOLVE_ERROR:
                jsonData.put("error", "Cannot be resolved on server");
                break;
            case TIMEOUT:
                jsonData.put("error", "No response/Timeout from server");
                break;
            case CONVERSION_ERROR:
                jsonData.put("error", "Conversion error");
                break;
            default:
                jsonData.put("error", "Unknown errors");
                break;
        }
        return jsonData;
    }

    /**
     * Creates a back-end url that can be passed on the API method calls, using the path.
     *
     * @param config the configuration settings.
     * @param path   the relative path to generate the full path from.
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
}
