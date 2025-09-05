package controllers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.Application;
import play.test.Helpers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class InstrumentControllerTest {
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
