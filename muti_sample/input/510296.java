public class PixelDensityQualifierTest extends TestCase {
    private PixelDensityQualifier pdq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pdq = new PixelDensityQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        pdq = null;
        config = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, pdq.checkAndSet("ldpi", config));
        assertTrue(config.getPixelDensityQualifier() != null);
        assertEquals(Density.LOW, config.getPixelDensityQualifier().getValue());
        assertEquals("ldpi", config.getPixelDensityQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, pdq.checkAndSet("", config));
        assertEquals(false, pdq.checkAndSet("dpi", config));
        assertEquals(false, pdq.checkAndSet("123dpi", config));
        assertEquals(false, pdq.checkAndSet("123", config));
        assertEquals(false, pdq.checkAndSet("sdfdpi", config));
    }
}
