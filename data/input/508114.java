@TestTargetClass(InvalidPreferencesFormatException.class)
public class InvalidPreferencesFormatExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidPreferencesFormatException",
        args = {java.lang.String.class}
    )
    public void testInvalidPreferencesFormatExceptionString() {
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
        "msg");
        assertNull(e.getCause());
        assertEquals("msg", e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidPreferencesFormatException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidPreferencesFormatExceptionStringThrowable() {
        Throwable t = new Throwable("root");
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
                "msg", t);
        assertSame(t, e.getCause());
        assertTrue(e.getMessage().indexOf("root") < 0);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) < 0);
        assertTrue(e.getMessage().indexOf("msg") >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidPreferencesFormatException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidPreferencesFormatExceptionThrowable() {
        Throwable t = new Throwable("root");
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
                t);
        assertSame(t, e.getCause());
        assertTrue(e.getMessage().indexOf("root") >= 0);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new InvalidPreferencesFormatException(
        "msg"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new InvalidPreferencesFormatException("msg"));
    }
}
