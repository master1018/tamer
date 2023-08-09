public class TextInputMethodQualifierTest extends TestCase {
    private TextInputMethodQualifier timq;
    private FolderConfiguration config;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        timq = new TextInputMethodQualifier();
        config = new FolderConfiguration();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        timq = null;
        config = null;
    }
    public void testQuerty() {
        assertEquals(true, timq.checkAndSet("qwerty", config)); 
        assertTrue(config.getTextInputMethodQualifier() != null);
        assertEquals(TextInputMethodQualifier.TextInputMethod.QWERTY,
                config.getTextInputMethodQualifier().getValue());
        assertEquals("qwerty", config.getTextInputMethodQualifier().toString()); 
    }
    public void test12Key() {
        assertEquals(true, timq.checkAndSet("12key", config)); 
        assertTrue(config.getTextInputMethodQualifier() != null);
        assertEquals(TextInputMethodQualifier.TextInputMethod.TWELVEKEYS,
                config.getTextInputMethodQualifier().getValue());
        assertEquals("12key", config.getTextInputMethodQualifier().toString()); 
    }
    public void testNoKey() {
        assertEquals(true, timq.checkAndSet("nokeys", config)); 
        assertTrue(config.getTextInputMethodQualifier() != null);
        assertEquals(TextInputMethodQualifier.TextInputMethod.NOKEY,
                config.getTextInputMethodQualifier().getValue());
        assertEquals("nokeys", config.getTextInputMethodQualifier().toString()); 
    }
    public void testFailures() {
        assertEquals(false, timq.checkAndSet("", config));
        assertEquals(false, timq.checkAndSet("QWERTY", config));
        assertEquals(false, timq.checkAndSet("12keys", config));
        assertEquals(false, timq.checkAndSet("*12key", config));
        assertEquals(false, timq.checkAndSet("other", config));
    }
}
