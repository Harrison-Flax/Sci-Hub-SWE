package controllers;

import play.mvc.*;
import views.html.*;

/**
 * This controller mainly handles all menu items under "About" menu, marked by a question mark.
 * Each menu item mainly renders a corresponding scala.html page.
 */

public class AboutController extends Controller {

    /**
     * This method renders aboutUs.scala.html
     * @return
     */
    public Result aboutUs() {
        return ok(aboutUs.render());
    }

    /**
     * This method renders aboutProject.scala.html
     * @return
     */
    public Result aboutProject(){
        return ok(aboutProject.render());
    }

    /**
     * This method renders frequentlyAskedQuestions.scala.html
     * @return
     */
    public Result frequentlyAskedQuestions() {
        return ok(frequentlyAskedQuestions.render());
    }

    /**
     * This method renders popularQueries.scala.html
     * @return
     */
    public Result popularQueries() {
        return ok(popularQueries.render());
    }



}
