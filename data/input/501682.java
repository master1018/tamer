@TestTargetClass(Namespace.class)
public class LocalSocketAddress_NamespaceTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test valueOf(String name).",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Namespace.ABSTRACT, Namespace.valueOf("ABSTRACT"));
        assertEquals(Namespace.RESERVED, Namespace.valueOf("RESERVED"));
        assertEquals(Namespace.FILESYSTEM, Namespace.valueOf("FILESYSTEM"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test values().",
        method = "values",
        args = {}
    )
    public void testValues() {
        Namespace[] expected = Namespace.values();
        assertEquals(Namespace.ABSTRACT, expected[0]);
        assertEquals(Namespace.RESERVED, expected[1]);
        assertEquals(Namespace.FILESYSTEM, expected[2]);
    }
}
