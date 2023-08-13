public class FailingTest extends TestCase {
    public void testFailOne() throws Exception {
        fail("Expected");
    }
    public void testFailTwo() throws Exception {
        fail("Expected");
    }
}
