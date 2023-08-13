@TestTargetClass(java.util.ListResourceBundle.class) 
public class ListResourceBundleTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getKeys",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ListResourceBundle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getContents",
            args = {}
        )
    })
    public void test_getKeys() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        Enumeration keys = bundle.getKeys();
        Vector result = new Vector();
        while (keys.hasMoreElements()) {
            result.addElement(keys.nextElement());
        }
        assertTrue("Missing key parent1", result.contains("parent1"));
        assertTrue("Missing key parent2", result.contains("parent2"));
        assertTrue("Missing key parent3", result.contains("parent3"));
        assertTrue("Missing key parent4", result.contains("parent4"));
        assertTrue("Missing key child1", result.contains("child1"));
        assertTrue("Missing key child2", result.contains("child2"));
        assertTrue("Missing key child3", result.contains("child3"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "handleGetObject",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ListResourceBundle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getContents",
            args = {}
        )
    })
    public void test_handleGetObjectLjava_lang_String() {
        ListResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = (ListResourceBundle)ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        Enumeration keys = bundle.getKeys();
        String keyValue = null;
        Vector result = new Vector();
        while (keys.hasMoreElements()) {
            result.addElement(bundle.handleGetObject((String)keys.nextElement()));
        }
        assertEquals(9, result.size());
        assertTrue(result.contains(null));
        assertTrue(result.contains("frFRVARValue4"));
        assertTrue(result.contains("frFRVARChildValue1"));
        assertTrue(result.contains("frFRVARChildValue2"));
        assertTrue(result.contains("frFRVARChildValue3"));
        assertTrue(result.remove(null));
        assertTrue(result.remove(null));
        assertTrue(result.remove(null));
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
