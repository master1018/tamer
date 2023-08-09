public class KeyboardStateQualifierTest extends TestCase {
    private KeyboardStateQualifier ksq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ksq = new KeyboardStateQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ksq = null;
        config = null;
    }
    public void testExposed() {
        assertEquals(true, ksq.checkAndSet("keysexposed", config)); 
        assertTrue(config.getKeyboardStateQualifier() != null);
        assertEquals(KeyboardStateQualifier.KeyboardState.EXPOSED,
                config.getKeyboardStateQualifier().getValue());
        assertEquals("keysexposed", config.getKeyboardStateQualifier().toString()); 
    }
    public void testHidden() {
        assertEquals(true, ksq.checkAndSet("keyshidden", config)); 
        assertTrue(config.getKeyboardStateQualifier() != null);
        assertEquals(KeyboardStateQualifier.KeyboardState.HIDDEN,
                config.getKeyboardStateQualifier().getValue());
        assertEquals("keyshidden", config.getKeyboardStateQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, ksq.checkAndSet("", config));
        assertEquals(false, ksq.checkAndSet("KEYSEXPOSED", config));
        assertEquals(false, ksq.checkAndSet("other", config));
    }
}
