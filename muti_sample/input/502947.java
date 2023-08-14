public class FlagTests extends TestCase {
    public void testFlagsUpperCase() {
        for (Flag flag : Flag.values()) {
            String name = flag.name();
            assertEquals(name.toUpperCase(), name);
        }
    }
}
