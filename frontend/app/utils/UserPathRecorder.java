package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import com.typesafe.config.Config;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class UserPathRecorder extends play.mvc.Action.Simple {

    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

        String id = ctx.session().get("id");
        String fromURL = ctx.session().get("fromURL") == null ? "Null" : ctx.session().get("fromURL");
        String toURL = ctx.args.get("ROUTE_PATTERN").toString();

        try {
            record(id, fromURL, toURL);
            Logger.info("User " + id + " from " + fromURL + " to " + toURL);
            ctx.session().put("fromURL", toURL);
        }
        catch (Exception e) {
            Logger.error(e.toString());
        }

        return delegate.call(ctx);
    }

    private void record(String id, String fromURL, String toURL) throws Exception{
        Map<String, String> map = new HashMap<>();
        map.put("fromURL", fromURL);
        map.put("toURL", toURL);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.valueToTree(map);
        JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.REGISTER_SITE_NAVIGATION_EVENT + id), json);
        Logger.info(response.asText());
    }
}
