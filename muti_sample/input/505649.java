public class NavigationMethodQualifierTest extends TestCase {
    private FolderConfiguration config;
    private NavigationMethodQualifier nmq;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        config = new FolderConfiguration();
        nmq = new NavigationMethodQualifier();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
        nmq = null;
    }
    public void testDPad() {
        assertEquals(true, nmq.checkAndSet("dpad", config)); 
        assertTrue(config.getNavigationMethodQualifier() != null);
        assertEquals(NavigationMethodQualifier.NavigationMethod.DPAD,
                config.getNavigationMethodQualifier().getValue());
        assertEquals("dpad", config.getNavigationMethodQualifier().toString()); 
    }
    public void testTrackball() {
        assertEquals(true, nmq.checkAndSet("trackball", config)); 
        assertTrue(config.getNavigationMethodQualifier() != null);
        assertEquals(NavigationMethodQualifier.NavigationMethod.TRACKBALL,
                config.getNavigationMethodQualifier().getValue());
        assertEquals("trackball", config.getNavigationMethodQualifier().toString()); 
    }
    public void testWheel() {
        assertEquals(true, nmq.checkAndSet("wheel", config)); 
        assertTrue(config.getNavigationMethodQualifier() != null);
        assertEquals(NavigationMethodQualifier.NavigationMethod.WHEEL,
                config.getNavigationMethodQualifier().getValue());
        assertEquals("wheel", config.getNavigationMethodQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, nmq.checkAndSet("", config));
        assertEquals(false, nmq.checkAndSet("WHEEL", config));
        assertEquals(false, nmq.checkAndSet("other", config));
    }
}
