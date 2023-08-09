public class ScreenOrientationQualifierTest extends TestCase {
    private ScreenOrientationQualifier soq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        soq = new ScreenOrientationQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        soq = null;
        config = null;
    }
    public void testPortrait() {
        assertEquals(true, soq.checkAndSet("port", config)); 
        assertTrue(config.getScreenOrientationQualifier() != null);
        assertEquals(ScreenOrientationQualifier.ScreenOrientation.PORTRAIT,
                config.getScreenOrientationQualifier().getValue());
        assertEquals("port", config.getScreenOrientationQualifier().toString()); 
    }
    public void testLanscape() {
        assertEquals(true, soq.checkAndSet("land", config)); 
        assertTrue(config.getScreenOrientationQualifier() != null);
        assertEquals(ScreenOrientationQualifier.ScreenOrientation.LANDSCAPE,
                config.getScreenOrientationQualifier().getValue());
        assertEquals("land", config.getScreenOrientationQualifier().toString()); 
    }
    public void testSquare() {
        assertEquals(true, soq.checkAndSet("square", config)); 
        assertTrue(config.getScreenOrientationQualifier() != null);
        assertEquals(ScreenOrientationQualifier.ScreenOrientation.SQUARE,
                config.getScreenOrientationQualifier().getValue());
        assertEquals("square", config.getScreenOrientationQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, soq.checkAndSet("", config));
        assertEquals(false, soq.checkAndSet("PORT", config));
        assertEquals(false, soq.checkAndSet("landscape", config));
        assertEquals(false, soq.checkAndSet("portrait", config));
        assertEquals(false, soq.checkAndSet("other", config));
    }
}
