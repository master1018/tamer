public class CountryCodeQualifierTest extends TestCase {
    private CountryCodeQualifier mccq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mccq = new CountryCodeQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mccq = null;
        config = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, mccq.checkAndSet("mcc123", config));
        assertTrue(config.getCountryCodeQualifier() != null);
        assertEquals(123, config.getCountryCodeQualifier().getCode());
        assertEquals("mcc123", config.getCountryCodeQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, mccq.checkAndSet("", config));
        assertEquals(false, mccq.checkAndSet("mcc", config));
        assertEquals(false, mccq.checkAndSet("MCC123", config));
        assertEquals(false, mccq.checkAndSet("123", config));
        assertEquals(false, mccq.checkAndSet("mccsdf", config));
    }
}
