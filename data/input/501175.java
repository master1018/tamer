public class FloatTest extends junit.framework.TestCase {
    public void test_valueOf_String1() throws Exception {
        assertEquals(2358.166016f, Float.valueOf("2358.166016"));
    }
    public void test_valueOf_String2() throws Exception {
        assertEquals(-2.14748365E9f, Float.valueOf(String.valueOf(Integer.MIN_VALUE)));
    }
}
