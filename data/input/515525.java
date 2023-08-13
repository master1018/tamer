public class NetworkCodeQualifierTest extends TestCase {
    private NetworkCodeQualifier mncq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mncq = new NetworkCodeQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mncq = null;
        config = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, mncq.checkAndSet("mnc123", config));
        assertTrue(config.getNetworkCodeQualifier() != null);
        assertEquals(123, config.getNetworkCodeQualifier().getCode());
        assertEquals("mnc123", config.getNetworkCodeQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, mncq.checkAndSet("", config));
        assertEquals(false, mncq.checkAndSet("mnc", config));
        assertEquals(false, mncq.checkAndSet("MNC123", config));
        assertEquals(false, mncq.checkAndSet("123", config));
        assertEquals(false, mncq.checkAndSet("mncsdf", config));
    }
}
