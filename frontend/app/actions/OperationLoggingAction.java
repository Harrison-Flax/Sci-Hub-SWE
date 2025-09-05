package actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class OperationLoggingAction extends play.mvc.Action.Simple {
    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        Map<String, Object> args = ctx.args;
        Http.Request req = ctx.request();
        JsonNode json = req.body().asJson();
        Http.Session session = ctx.session();
        String logKey;
        if (null != json && null != json.get("logKey")) logKey = json.get("logKey").asText();
        else logKey = UUID.randomUUID().toString();
        String channel = "FRONTEND";
        String userId = session.get("id");
        long timestamp = System.currentTimeMillis();
        String ip = req.header("X-Real-IP").toString();
//        if ("unknown".equalsIgnoreCase(ip))
//            ip = req.header("X-Forwarded-For").toString();
//        if ("unknown".equalsIgnoreCase(ip));
        Logger.debug(ip);
        ip = req.remoteAddress();
        String operationPattern = args.get("ROUTE_PATTERN").toString();
        String operationMethod = args.get("ROUTE_ACTION_METHOD").toString();
        String operationController = args.get("ROUTE_CONTROLLER").toString();
        String operationComments = "User " + userId + " requests " + operationPattern + " at " + timestamp;

        Map<String, String> logData = new HashMap<>();
        ObjectNode jsonData = (ObjectNode)(Json.toJson(logData));
        jsonData.put("logKey", logKey);
        jsonData.put("channel", channel);
        jsonData.put("userId", userId);
        jsonData.put("timestamp", timestamp);
        jsonData.put("ip", ip);
        jsonData.put("operationPattern", operationPattern);
        jsonData.put("operationMethod", operationMethod);
        jsonData.put("operationController", operationController);
        jsonData.put("operationComments", operationComments);

        JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.STR_OPERATION_LOGGING), jsonData);

        return delegate.call(ctx);
    }
}
