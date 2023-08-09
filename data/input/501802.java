@TestTargetClass(ECGenParameterSpec.class)
public class ECGenParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECGenParameterSpec",
        args = {java.lang.String.class}
    )
    public final void testECGenParameterSpec01() {
        new ECGenParameterSpec("someName");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ECGenParameterSpec",
        args = {java.lang.String.class}
    )
    public final void testECGenParameterSpec02() {
        try {
            new ECGenParameterSpec(null);
            fail("NPE expected");
        } catch (NullPointerException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public final void testGetName() {
        String name = "someName";
        ECGenParameterSpec ps = new ECGenParameterSpec(name);
        assertEquals(name, ps.getName());
    }
}
