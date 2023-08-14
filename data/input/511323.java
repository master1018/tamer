@TestTargetClass(CoderMalfunctionError.class)
public class CoderMalfunctionErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CoderMalfunctionError",
        args = {java.lang.Exception.class}
    )
    public void testConstructor_Normal() {
        Exception ex = new Exception();
        CoderMalfunctionError e = new CoderMalfunctionError(ex);
        assertSame(ex, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CoderMalfunctionError",
        args = {java.lang.Exception.class}
    )
    public void testConstructor_Null() {
        CoderMalfunctionError e = new CoderMalfunctionError(null);
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new CoderMalfunctionError(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new CoderMalfunctionError(null));
    }
}
