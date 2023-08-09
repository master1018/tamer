@TestTargetClass(Class.class) 
public class ClassTest2 extends junit.framework.TestCase {
    protected void setUp() {
    }
    protected void tearDown() {
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getResourceAsStream",
        args = {java.lang.String.class}
    )
    public void testGetResourceAsStream1() throws IOException {
        Class clazz = getClass();
        InputStream stream = clazz.getResourceAsStream("HelloWorld.txt");
        assert(stream != null);
        byte[] buffer = new byte[20];
        int length = stream.read(buffer);
        String s = new String(buffer, 0, length);
        assert("Hello, World.".equals(s));
        stream.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getResourceAsStream",
        args = {java.lang.String.class}
    )
    public void testGetResourceAsStream2() throws IOException {
        Class clazz = getClass();
        InputStream stream = clazz.getResourceAsStream("/org/apache/harmony/luni/tests/java/lang/HelloWorld.txt");
        assert(stream != null);
        byte[] buffer = new byte[20];
        int length = stream.read(buffer);
        String s = new String(buffer, 0, length);
        assert("Hello, World.".equals(s));
        stream.close();
        try {
            clazz.getResourceAsStream(null);
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
        }
        assertNull(clazz.getResourceAsStream("/NonExistentResource"));
        assertNull(clazz.getResourceAsStream("org/apache/harmony/luni/tests/java/lang/HelloWorld.txt"));
    }
}
