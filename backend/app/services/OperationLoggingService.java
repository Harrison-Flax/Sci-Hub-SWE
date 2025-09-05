package services;

import models.OperationLog;

public class OperationLoggingService {
    public boolean recordOperationLog(OperationLog operationLog) {
        try {
            operationLog.save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean recordOperationLog(
            String logKey, OperationLog.CHANNEL_TYPE channel, Long userId, Long timestamp, String ip, String routePattern, String actionMethod, String controller, String comment
    ) {
        OperationLog operationLog = new OperationLog(
                logKey, channel, userId, timestamp, ip, routePattern, actionMethod, controller, comment
        );
        return recordOperationLog(operationLog);
    }
}
