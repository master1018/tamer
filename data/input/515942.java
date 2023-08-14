public class SystemPropertiesTest extends TestCase {
    private static final String KEY = "com.android.frameworks.coretests";
    @SmallTest
    public void testProperties() throws Exception {
        if (false) {
        String value;
        SystemProperties.set(KEY, "");
        value = SystemProperties.get(KEY, "default");
        assertEquals("default", value);
        SystemProperties.set(KEY, "AAA");
        value = SystemProperties.get(KEY, "default");
        assertEquals("AAA", value);
        value = SystemProperties.get(KEY);
        assertEquals("AAA", value);
        SystemProperties.set(KEY, "");
        value = SystemProperties.get(KEY, "default");
        assertEquals("default", value);
        value = SystemProperties.get(KEY);
        assertEquals("", value);
        }
    }
}
