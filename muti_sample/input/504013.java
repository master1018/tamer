public class DxAbstractMain {
    static public void assertEquals(int expected, int actual) {
        if (expected != actual) throw new RuntimeException("AssertionFailedError: not equals");
    }
    static public void assertEquals(long expected, long actual) {
        if (expected != actual) throw new RuntimeException("AssertionFailedError: not equals");
    }
    static public void assertEquals(double expected, double actual, double delta) {
        if(!(Math.abs(expected-actual) <= delta)) throw new RuntimeException("AssertionFailedError: not within delta");
    }
    static public void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null)
            return;
        if (expected != null && expected.equals(actual))
            return;
        throw new RuntimeException("AssertionFailedError: not the same");
    }
    static public void assertTrue(boolean condition) {
        if (!condition) throw new RuntimeException("AssertionFailedError: condition was false");
    }
    static public void assertFalse(boolean condition) {
        if (condition) throw new RuntimeException("AssertionFailedError: condition was true");
    }
    static public void assertNotNull(Object object) {
        if (object == null) throw new RuntimeException("AssertionFailedError: object was null");
    }
    static public void assertNull(Object object) {
        if (object != null) throw new RuntimeException("AssertionFailedError: object was not null");
    }
    static public void fail(String message) {
        throw new RuntimeException("AssertionFailedError msg:"+message);
    }
}
