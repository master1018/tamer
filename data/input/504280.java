@TestTargetClass(UnsupportedOperationException.class) 
public class UnsupportedOperationExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedOperationException",
        args = {}
    )
    public void test_Constructor() {
        UnsupportedOperationException e = new UnsupportedOperationException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedOperationException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        UnsupportedOperationException e = new UnsupportedOperationException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedOperationException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_ConstructorLStringLThrowable() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        UnsupportedOperationException uoe = new UnsupportedOperationException(message, npe);
        assertEquals(message, uoe.getMessage());
        assertEquals(npe, uoe.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedOperationException",
        args = {java.lang.Throwable.class}
    )
    public void test_ConstructorLThrowable(){
        NullPointerException npe = new NullPointerException();
        UnsupportedOperationException uoe = new UnsupportedOperationException(npe);
        assertEquals(npe, uoe.getCause());
        uoe = new UnsupportedOperationException((Throwable) null);
        assertNull("The cause is not null.", uoe.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new UnsupportedOperationException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new UnsupportedOperationException());
    }
}
