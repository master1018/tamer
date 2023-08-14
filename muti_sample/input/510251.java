public class SdkCommandLineTest extends TestCase {
    private MockStdLogger mLog;
    public static class MockSdkCommandLine extends SdkCommandLine {
        private boolean mExitCalled;
        private boolean mHelpCalled;
        public MockSdkCommandLine(ISdkLog logger) {
            super(logger);
        }
        @Override
        public void printHelpAndExitForAction(String verb, String directObject,
                String errorFormat, Object... args) {
            mHelpCalled = true;
            super.printHelpAndExitForAction(verb, directObject, errorFormat, args);
        }
        @Override
        protected void exit() {
            mExitCalled = true;
        }
        @Override
        protected void stdout(String format, Object... args) {
        }
        @Override
        protected void stderr(String format, Object... args) {
        }
        public boolean wasExitCalled() {
            return mExitCalled;
        }
        public boolean wasHelpCalled() {
            return mHelpCalled;
        }
    }
    @Override
    protected void setUp() throws Exception {
        mLog = new MockStdLogger();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public final void testList_Avd_Verbose() {
        MockSdkCommandLine c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "-v", "list", "avd" });
        assertFalse(c.wasHelpCalled());
        assertFalse(c.wasExitCalled());
        assertEquals("list", c.getVerb());
        assertEquals("avd", c.getDirectObject());
        assertTrue(c.isVerbose());
    }
    public final void testList_Target() {
        MockSdkCommandLine c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "list", "target" });
        assertFalse(c.wasHelpCalled());
        assertFalse(c.wasExitCalled());
        assertEquals("list", c.getVerb());
        assertEquals("target", c.getDirectObject());
        assertFalse(c.isVerbose());
    }
    public final void testList_None() {
        MockSdkCommandLine c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "list" });
        assertFalse(c.wasHelpCalled());
        assertFalse(c.wasExitCalled());
        assertEquals("list", c.getVerb());
        assertEquals("", c.getDirectObject());
        assertFalse(c.isVerbose());
    }
    public final void testList_Invalid() {
        MockSdkCommandLine c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "list", "unknown" });
        assertTrue(c.wasHelpCalled());
        assertTrue(c.wasExitCalled());
        assertEquals(null, c.getVerb());
        assertEquals(null, c.getDirectObject());
        assertFalse(c.isVerbose());
    }
    public final void testList_Plural() {
        MockSdkCommandLine c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "list", "avds" });
        assertFalse(c.wasHelpCalled());
        assertFalse(c.wasExitCalled());
        assertEquals("list", c.getVerb());
        assertEquals("avd", c.getDirectObject());
        assertFalse(c.isVerbose());
        c = new MockSdkCommandLine(mLog);
        c.parseArgs(new String[] { "list", "targets" });
        assertFalse(c.wasHelpCalled());
        assertFalse(c.wasExitCalled());
        assertEquals("list", c.getVerb());
        assertEquals("target", c.getDirectObject());
        assertFalse(c.isVerbose());
    }
}
