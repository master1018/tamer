public class InstrumentationFailToRunTest extends TestCase {
    public void testInstrumentationNotAllowed() {
        fail("instrumentating app with different cert should fail");
    }
}
