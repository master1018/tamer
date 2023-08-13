@TestTargetClass(BackingStoreException.class)
public class BackingStoreExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BackingStoreException",
        args = {java.lang.String.class}
    )
    public void testBackingStoreExceptionString() {
        BackingStoreException e = new BackingStoreException("msg");
        assertNull(e.getCause());
        assertEquals("msg", e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BackingStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testBackingStoreExceptionThrowable() {
        Throwable t = new Throwable("msg");
        BackingStoreException e = new BackingStoreException(t);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) >= 0);
        assertTrue(e.getMessage().indexOf("msg") >= 0);
        assertEquals(t, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new BackingStoreException("msg"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new BackingStoreException("msg"));
    }
}
