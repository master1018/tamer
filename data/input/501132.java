@TestTargetClass(IllegalStateException.class) 
public class IllegalStateExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalStateException",
        args = {}
    )
    public void test_Constructor() {
        IllegalStateException e = new IllegalStateException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalStateException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalStateException e = new IllegalStateException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalStateException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_ConstructorLjava_lang_StringLThrowable() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        IllegalStateException e = new IllegalStateException(message, npe);
        assertEquals(message, e.getMessage());
        assertEquals(npe, e.getCause());
        e = new IllegalStateException(message, null);
        assertEquals(message, e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalStateException",
        args = {java.lang.Throwable.class}
    )
    public void test_ConstructorLThrowable() {
      NullPointerException npe = new NullPointerException();
      IllegalStateException e = new IllegalStateException(npe);
      assertEquals(npe, e.getCause());
      e = new IllegalStateException((Throwable)null);
      assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new IllegalStateException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new IllegalStateException());
    }
}
