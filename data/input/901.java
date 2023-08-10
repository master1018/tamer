public class SweetSuite extends JFuncTestCase {
    boolean sharing = false;
    public SweetSuite() {
        super("NONE");
    }
    public SweetSuite(String name) {
        super(name);
    }
    public void testPassed() {
        assert("This will never fail", true);
    }
    public void testFailed() {
        fail("This could never succeed");
    }
    public void testErrored() {
        throw new RuntimeException("This is always error here");
    }
    public void testArgs(String msg) {
        assert("Msg does not equal null", msg != null);
    }
    public void testSharing() {
        assert("sharing should always start out false", sharing == false);
        sharing = true;
    }
    public static Test suite() {
        try {
            JFuncSuite suite = new JFuncSuite();
            suite.oneTest(true);
            SweetSuite realTestInstance = new SweetSuite();
            SweetSuite testProxy = 
                (SweetSuite) suite.getTestProxy(realTestInstance);
            testProxy.testPassed();
            testProxy.testFailed();
            testProxy.testErrored();
            testProxy.testArgs("testing 1, 2, 3");
            testProxy.testArgs(null);
            testProxy.testSharing();
            testProxy.testSharing();
            return suite;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
