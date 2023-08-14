public class MethodTest extends junit.framework.TestCase {
    public void test_getExceptionTypes() throws Exception {
        Method method = MethodTestHelper.class.getMethod("m1", new Class[0]);
        Class[] exceptions = method.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
        exceptions[0] = NullPointerException.class;
        exceptions = method.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
    }
    public void test_getParameterTypes() throws Exception {
        Class[] expectedParameters = new Class[] { Object.class };
        Method method = MethodTestHelper.class.getMethod("m2", expectedParameters);
        Class[] parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        parameters[0] = String.class;
        parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }
    static class MethodTestHelper {
        public void m1() throws IndexOutOfBoundsException { }
        public void m2(Object o) { }
    }
}
