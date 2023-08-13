public class ServiceLocator {
    private static TestBrowserController mTestBrowserController =
            new TestBrowserControllerImpl();
    public static TestBrowserController getTestBrowserController() {
        return mTestBrowserController;
    }
    static void setTestBrowserController(TestBrowserController testBrowserController) {
        mTestBrowserController = testBrowserController;
    }
}
