public class TouchScreenQualifierTest extends TestCase {
    private TouchScreenQualifier tsq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tsq = new TouchScreenQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        tsq = null;
        config = null;
    }
    public void testNoTouch() {
        assertEquals(true, tsq.checkAndSet("notouch", config)); 
        assertTrue(config.getTouchTypeQualifier() != null);
        assertEquals(TouchScreenQualifier.TouchScreenType.NOTOUCH,
                config.getTouchTypeQualifier().getValue());
        assertEquals("notouch", config.getTouchTypeQualifier().toString()); 
    }
    public void testFinger() {
        assertEquals(true, tsq.checkAndSet("finger", config)); 
        assertTrue(config.getTouchTypeQualifier() != null);
        assertEquals(TouchScreenQualifier.TouchScreenType.FINGER,
                config.getTouchTypeQualifier().getValue());
        assertEquals("finger", config.getTouchTypeQualifier().toString()); 
    }
    public void testStylus() {
        assertEquals(true, tsq.checkAndSet("stylus", config)); 
        assertTrue(config.getTouchTypeQualifier() != null);
        assertEquals(TouchScreenQualifier.TouchScreenType.STYLUS,
                config.getTouchTypeQualifier().getValue());
        assertEquals("stylus", config.getTouchTypeQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, tsq.checkAndSet("", config));
        assertEquals(false, tsq.checkAndSet("STYLUS", config));
        assertEquals(false, tsq.checkAndSet("other", config));
    }
}
