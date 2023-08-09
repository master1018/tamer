public class ConstructorTest extends junit.framework.TestCase {
    public void test_getExceptionTypes() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(new Class[0]);
        Class[] exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
        exceptions[0] = NullPointerException.class;
        exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
    }
    public void test_getParameterTypes() throws Exception {
        Class[] expectedParameters = new Class[] { Object.class };
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        Class[] parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        parameters[0] = String.class;
        parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }
    static class ConstructorTestHelper {
        public ConstructorTestHelper() throws IndexOutOfBoundsException { }
        public ConstructorTestHelper(Object o) { }
    }
}
