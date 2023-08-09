public class PartiallySuppressedTest extends TestCase {
    @Suppress
    public void testSuppressedMethod() throws Exception {
        assertTrue(true);
    }
    public void testUnSuppressedMethod() throws Exception {
        assertTrue(true);
    }
}
