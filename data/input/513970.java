public class LanguageQualifierTest extends TestCase {
    private FolderConfiguration config;
    private LanguageQualifier lq;
    @Override
    public void setUp()  throws Exception {
        super.setUp();
        config = new FolderConfiguration();
        lq = new LanguageQualifier();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
        lq = null;
    }
    public void testCheckAndSet() {
        assertEquals(true, lq.checkAndSet("en", config)); 
        assertTrue(config.getLanguageQualifier() != null);
        assertEquals("en", config.getLanguageQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, lq.checkAndSet("", config)); 
        assertEquals(false, lq.checkAndSet("EN", config)); 
        assertEquals(false, lq.checkAndSet("abc", config)); 
    }
}
