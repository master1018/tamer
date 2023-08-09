@TestTargetClass(IllegalArgumentException.class) 
public class IllegalArgumentExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalArgumentException",
        args = {}
    )
    public void test_Constructor() {
        IllegalArgumentException e = new IllegalArgumentException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalArgumentException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        IllegalArgumentException e = new IllegalArgumentException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalArgumentException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        IllegalArgumentException e = new IllegalArgumentException("fixture",
                npe);
        assertSame("fixture", e.getMessage());
        assertSame(npe, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalArgumentException",
        args = {java.lang.Throwable.class}
    )
    public void test_ConstructorLjava_lang_Throwable() {
              NullPointerException npe = new NullPointerException();
              IllegalArgumentException e = new IllegalArgumentException(npe);
              assertSame(npe, e.getCause());
    }    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new IllegalArgumentException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new IllegalArgumentException());
    }
}
