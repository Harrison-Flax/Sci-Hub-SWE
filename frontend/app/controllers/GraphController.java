package controllers;

import javax.inject.*;
import play.mvc.*;
import views.html.*;

public class GraphController extends Controller {

    //researcher interest graph
    public Result researchInterest() {
        return ok(researchInterest.render());
    }
}
