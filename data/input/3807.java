public abstract class ATestCaseScaffold {
    private String      fName;
    private boolean     fVerbose;
    protected
    ATestCaseScaffold(String name) {
        fName = name;
        fVerbose = false;
    }
    public final void
    runTest()
        throws Throwable {
        Throwable toRethrow = null;
        setUp();
        try {
            doRunTest();
        }
        finally {
            tearDown();
        }
    }
    protected void
    setUp()
        throws Exception {
    }
    protected void
    tearDown()
        throws Exception {
    }
    protected abstract void
    doRunTest()
        throws Throwable;
    public void
    beVerbose()
    {
        fVerbose = true;
    }
    public void
    verbosePrint(String message)
    {
        if (fVerbose)
        {
            System.out.println("Debugging message: " + message);
        }
    }
    public final void
    fail() {
        throw new TestCaseScaffoldException();
    }
    public final void
    fail(String message) {
        throw new TestCaseScaffoldException(message);
    }
    public final void
    assertTrue(boolean condition) {
        if ( !condition ) {
            fail();
        }
    }
    public final void
    assertTrue(String message, boolean condition) {
        if ( !condition ) {
            fail(message);
        }
    }
    public final void
    assertNotNull(Object o) {
        assertTrue(o != null);
    }
    public final void
    assertNotNull(String message, Object o) {
        assertTrue(message, o != null);
    }
    public final void
    assertEquals(String message, Object expected, Object actual) {
        if ( (expected == null) && (actual == null) ) {
            return;
        }
        else if ( (expected != null) && (expected.equals(actual)) ) {
            return;
        }
        else {
            throw new TestCaseScaffoldException(message + ". Expected: '" + expected +
                                                "'. Actual: '" + actual + "'.");
        }
    }
    public final void
    assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }
    public final void
    assertEquals(String message, int expected, int actual) {
        assertEquals(message, new Integer(expected), new Integer(actual));
    }
    public final void
    assertEquals(int expected, int actual) {
        assertEquals("Expected equality", expected, actual);
    }
    public final static class
    TestCaseScaffoldException extends RuntimeException {
        public
        TestCaseScaffoldException() {
            super();
        }
        public
        TestCaseScaffoldException(String m) {
            super(m);
        }
    }
}
