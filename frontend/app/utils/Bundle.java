package utils;

import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.net.URI;

import play.Logger;
import play.mvc.Http;

/**
 * Created by Gerald on 11/5/2016.
 */
public class Bundle extends HashMap<String, String> {

    public Bundle() {
        super();
    }

    public Bundle(Http.Request request) {
        try {
            String uri = request.path() + request.uri();
            Bundle result = this;
            List<NameValuePair> ret = URLEncodedUtils.parse(new URI(uri), "UTF-8");
            for(int i = 0; i < ret.size(); ++i)
                result.put(ret.get(i).getName(), ret.get(i).getValue());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * This utility method parses Request and prepare a list of (name, value) pairs.
     * @param request
     * @return
     */
    public static Bundle parseGetRequest(Http.Request request) {
        try {
            String uri = request.path() + request.uri();
            Bundle result = new Bundle();
            List<NameValuePair> pairList = URLEncodedUtils.parse(new URI(uri), "UTF-8");
            for (int i = 0; i < pairList.size(); ++i) {
                result.put(pairList.get(i).getName(), pairList.get(i).getValue());
                Logger.debug("name:" + pairList.get(i).getName() + "  +++ value: " + pairList.get(i).getValue());
            }
            return result;
        } catch(Exception e) {
            Logger.debug("utils.Bundle.parseGetRequest exception: " + e.toString());
            return null;
        }
    }
}
