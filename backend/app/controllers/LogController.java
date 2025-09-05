package controllers;

import actions.OperationLoggingAction;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.OperationLoggingService;

public class LogController extends Controller {
    @Inject
    Config config;

    private final OperationLoggingService operationLoggingService;

    @Inject
    public LogController(OperationLoggingService operationLoggingService) {
        this.operationLoggingService = operationLoggingService;
    }

    @With(OperationLoggingAction.class)
    public Result operationLogging() { return ok(); }
}
