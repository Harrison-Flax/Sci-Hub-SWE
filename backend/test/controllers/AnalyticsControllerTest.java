package controllers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.Application;
import play.test.Helpers;

public class AnalyticsControllerTest {
    static Application application;

    @BeforeClass
    public static void setupTestDB() {
        application = Helpers.fakeApplication();
        Helpers.start(application);
    }

    @AfterClass
    public static void tearDown() {
        // Call all the functionalities that are needed before the tests are finished.
        Helpers.stop(application);

    }
}
