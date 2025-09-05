package actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.OperationLog;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import services.OperationLoggingService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class OperationLoggingAction extends play.mvc.Action.Simple {
    private final OperationLoggingService operationLoggingService;

    @Inject
    public OperationLoggingAction(OperationLoggingService operationLoggingService) {
        this.operationLoggingService = operationLoggingService;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        Map<String, Object> args = ctx.args;

        Http.Request req = ctx.request();
        JsonNode json = req.body().asJson();
        String logKey, ip, operationPattern, operationMethod, operationController, operationComments;
        Long userId =  null, timestamp;
        OperationLog.CHANNEL_TYPE channel;

        if (null != json && null != json.get("logKey")) logKey = json.get("logKey").asText();
        else logKey = UUID.randomUUID().toString();

        if (null != json && null != json.get("channel")) channel = OperationLog.CHANNEL_TYPE.valueOf(json.get("channel").asText());
        else channel = OperationLog.CHANNEL_TYPE.BACKEND;

//        if (null != json && null != json.get("userId")) userId = Long.valueOf(json.get("userId").asText());
        if (json != null && json.has("userId")) {
            String userIdStr = json.get("userId").asText();
            try {
                if (userIdStr != null && !userIdStr.trim().isEmpty() && !"null".equalsIgnoreCase(userIdStr)) {
                    userId = Long.valueOf(userIdStr);
                }
            } catch (NumberFormatException e) {
                Logger.warn("Invalid userId format: " + userIdStr);
                userId = null;
            }
        }
//        if (null == userId) userId = Long.valueOf(Http.Context.current().session().get("id"));

        if (null != json && null != json.get("timestamp")) timestamp = Long.valueOf(json.get("timestamp").asText());
        else timestamp = System.currentTimeMillis();

        if (null != json && null != json.get("ip")) ip = json.get("ip").asText();
        else ip = req.remoteAddress();

        if (null != json && null != json.get("operationPattern")) operationPattern = json.get("operationPattern").asText();
        else operationPattern = args.get("ROUTE_PATTERN").toString();

        if (null != json && null != json.get("operationMethod")) operationMethod = json.get("operationMethod").asText();
        else operationMethod = args.get("ROUTE_ACTION_METHOD").toString();

        if (null != json && null != json.get("operationController")) operationController = json.get("operationController").asText();
        else operationController = args.get("ROUTE_CONTROLLER").toString();

        if (null != json && null != json.get("operationComments")) operationComments = json.get("operationComments").asText();
        else operationComments = "User " + userId + " requests " + operationPattern + " at " + timestamp;

//        OperationLog operationLog = new OperationLog(
//                logKey, channel, userId, timestamp, ip, operationPattern, operationMethod, operationController, operationComments
//        );

//        boolean recorded = operationLoggingService.recordOperationLog(operationLog);

        boolean recorded = operationLoggingService.recordOperationLog(logKey, channel, userId, timestamp, ip, operationPattern, operationMethod, operationController, operationComments);

        return delegate.call(ctx);
    }
}
