@TestTargetClass(LoggingPermission.class) 
public class LoggingPermissionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new LoggingPermission("control", ""));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new LoggingPermission("control",
                ""));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LoggingPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testLoggingPermission() {
        try {
            new LoggingPermission(null, null);
            fail("should throw IllegalArgumentException");
        } catch (NullPointerException e) {
        }
        try {
            new LoggingPermission("", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("bad name", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("Control", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("control",
                    "bad action");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        new LoggingPermission("control", "");
        new LoggingPermission("control", null);
    }
}
