public class ScreenDimensionQualifierTest extends TestCase {
    private ScreenDimensionQualifier sdq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sdq = new ScreenDimensionQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        sdq = null;
        config = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, sdq.checkAndSet("400x200", config));
        assertTrue(config.getScreenDimensionQualifier() != null);
        assertEquals(400, config.getScreenDimensionQualifier().getValue1());
        assertEquals(200, config.getScreenDimensionQualifier().getValue2());
        assertEquals("400x200", config.getScreenDimensionQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, sdq.checkAndSet("", config));
        assertEquals(false, sdq.checkAndSet("400X200", config));
        assertEquals(false, sdq.checkAndSet("x200", config));
        assertEquals(false, sdq.checkAndSet("ax200", config));
        assertEquals(false, sdq.checkAndSet("400x", config));
        assertEquals(false, sdq.checkAndSet("400xa", config));
        assertEquals(false, sdq.checkAndSet("other", config));
    }
}
