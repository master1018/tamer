@TestTargetClass(SecurityException.class) 
public class SecurityExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {}
    )
    public void test_Constructor() {
        SecurityException e = new SecurityException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        SecurityException e = new SecurityException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException("fixture", npe);
        assertSame("fixture", e.getMessage());
        assertSame(npe, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException(npe);
        assertSame(npe, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new SecurityException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new SecurityException());
    }
}
