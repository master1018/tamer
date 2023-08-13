public class RegionQualifierTest extends TestCase {
    private RegionQualifier rq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rq = new RegionQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        rq = null;
        config = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, rq.checkAndSet("rUS", config));
        assertTrue(config.getRegionQualifier() != null);
        assertEquals("US", config.getRegionQualifier().getValue()); 
        assertEquals("rUS", config.getRegionQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, rq.checkAndSet("", config));
        assertEquals(false, rq.checkAndSet("rus", config));
        assertEquals(false, rq.checkAndSet("rUSA", config));
        assertEquals(false, rq.checkAndSet("abc", config));
    }
}
