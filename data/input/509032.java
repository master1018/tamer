@TestTargetClass(FactoryConfigurationError.class) 
public class FactoryConfigurationErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FactoryConfigurationError",
        args = {}
    )
    public void test_Constructor() {
        FactoryConfigurationError fce = new FactoryConfigurationError();
        assertNull(fce.getMessage());
        assertNull(fce.getLocalizedMessage());
        assertNull(fce.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FactoryConfigurationError",
        args = {java.lang.Exception.class}
    )
    public void test_ConstructorLjava_lang_Exception() {
        Exception e = new Exception();
        FactoryConfigurationError fce = new FactoryConfigurationError(e);
        assertNotNull(fce.getMessage());
        assertNotNull(fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
        e = new Exception("test message");
        fce = new FactoryConfigurationError(e);
        assertEquals(e.toString(), fce.getMessage());
        assertEquals(e.toString(), fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FactoryConfigurationError",
        args = {java.lang.Exception.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_ExceptionLjava_lang_String() {
        Exception e = new Exception();
        FactoryConfigurationError fce = new FactoryConfigurationError(e, "msg");
        assertNotNull(fce.getMessage());
        assertNotNull(fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
        e = new Exception("test message");
        fce = new FactoryConfigurationError(e, "msg");
        assertEquals("msg", fce.getMessage());
        assertEquals("msg", fce.getLocalizedMessage());
        assertEquals(e.getCause(), fce.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FactoryConfigurationError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        FactoryConfigurationError fce = new FactoryConfigurationError("Oops!");
        assertEquals("Oops!", fce.getMessage());
        assertEquals("Oops!", fce.getLocalizedMessage());
        assertNull(fce.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void test_getException() {
        FactoryConfigurationError fce = new FactoryConfigurationError();
        assertNull(fce.getException());
        fce = new FactoryConfigurationError("test");
        assertNull(fce.getException());
        Exception e = new Exception("msg");
        fce = new FactoryConfigurationError(e);
        assertEquals(e, fce.getException());
        NullPointerException npe = new NullPointerException();
        fce = new FactoryConfigurationError(npe);
        assertEquals(npe, fce.getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        assertNull(new FactoryConfigurationError().getMessage());
        assertEquals("msg1",
                new FactoryConfigurationError("msg1").getMessage());
        assertEquals(new Exception("msg2").toString(),
                new FactoryConfigurationError(
                        new Exception("msg2")).getMessage());
        assertEquals(new NullPointerException().toString(),
                new FactoryConfigurationError(
                        new NullPointerException()).getMessage());
    }
}
