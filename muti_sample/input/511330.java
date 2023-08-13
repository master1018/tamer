@TestTargetClass(MissingResourceException.class) 
public class MissingResourceExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MissingResourceException",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_String() {
        assertNotNull(new MissingResourceException("Detail string", "Class name string", "Key string"));
        assertNotNull(new MissingResourceException(null, "Class name string", "Key string"));
        assertNotNull(new MissingResourceException("Detail string", null, "Key string"));
        assertNotNull(new MissingResourceException("Detail string", "Class name string", null));
        try {
            ResourceBundle.getBundle("Non-ExistentBundle");
        } catch (MissingResourceException e) {
            return;
        }
        fail("Failed to generate expected exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClassName",
        args = {}
    )
    public void test_getClassName() {
        try {
            ResourceBundle.getBundle("Non-ExistentBundle");
        } catch (MissingResourceException e) {
            assertEquals("Returned incorrect class name", "Non-ExistentBundle"
                    + '_' + Locale.getDefault(), e.getClassName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKey",
        args = {}
    )
    public void test_getKey() {
        try {
            ResourceBundle.getBundle("Non-ExistentBundle");
        } catch (MissingResourceException e) {
            assertTrue("Returned incorrect class name", e.getKey().equals(""));
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
