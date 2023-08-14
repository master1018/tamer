public class ErrorTest extends TestCase {
    public void testErrorOne() throws Exception {
        throw new RuntimeException("Expected");
    }
    public void testErrorTwo() throws Exception {
        throw new RuntimeException("Expected");
    }
}
