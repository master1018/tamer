public class AttrsXmlParserTest extends TestCase {
    private AttrsXmlParser mParser;
    private String mFilePath;
    private static final String MOCK_DATA_PATH =
        "com/android/ide/eclipse/testdata/mock_attrs.xml"; 
    @Override
    public void setUp() throws Exception {
        mFilePath = AdtTestData.getInstance().getTestFilePath(MOCK_DATA_PATH); 
        mParser = new AttrsXmlParser(mFilePath);
    }
    @Override
    public void tearDown() throws Exception {
    }
    public final void testGetDocument() throws Exception {
        assertNotNull(_getDocument());
    }
    public void testGetOsAttrsXmlPath() throws Exception {
        assertEquals(mFilePath, mParser.getOsAttrsXmlPath());
    }
    public final void testPreload() throws Exception {
        assertSame(mParser, mParser.preload());
    }
    public final void testLoadViewAttributes() throws Exception {
        mParser.preload();
        ViewClassInfo info = new ViewClassInfo(
                false ,
                "mock_android.something.Theme",      
                "Theme");                            
        mParser.loadViewAttributes(info);
        assertEquals("These are the standard attributes that make up a complete theme.", 
                info.getJavaDoc());
        AttributeInfo[] attrs = info.getAttributes();
        assertEquals(1, attrs.length);
        assertEquals("scrollbarSize", info.getAttributes()[0].getName());
        assertEquals(1, info.getAttributes()[0].getFormats().length);
        assertEquals(Format.DIMENSION, info.getAttributes()[0].getFormats()[0]);
    }
    public final void testEnumFlagValues() throws Exception {
        mParser.preload();
        Map<String, Map<String, Integer>> attrMap = mParser.getEnumFlagValues();
        assertTrue(attrMap.containsKey("orientation"));
        Map<String, Integer> valueMap = attrMap.get("orientation");
        assertTrue(valueMap.containsKey("horizontal"));
        assertTrue(valueMap.containsKey("vertical"));
        assertEquals(Integer.valueOf(0), valueMap.get("horizontal"));
        assertEquals(Integer.valueOf(1), valueMap.get("vertical"));
    }
    public final void testDeprecated() throws Exception {
        mParser.preload();
        DeclareStyleableInfo dep = mParser.getDeclareStyleableList().get("DeprecatedTest");
        assertNotNull(dep);
        AttributeInfo[] attrs = dep.getAttributes();
        assertEquals(4, attrs.length);
        assertEquals("deprecated-inline", attrs[0].getName());
        assertEquals("In-line deprecated.", attrs[0].getDeprecatedDoc());
        assertEquals("Deprecated comments using delimiters.", attrs[0].getJavaDoc());
        assertEquals("deprecated-multiline", attrs[1].getName());
        assertEquals("Multi-line version of deprecated that works till the next tag.",
                attrs[1].getDeprecatedDoc());
        assertEquals("Deprecated comments on their own line.", attrs[1].getJavaDoc());
        assertEquals("deprecated-not", attrs[2].getName());
        assertEquals(null, attrs[2].getDeprecatedDoc());
        assertEquals("This attribute is not deprecated.", attrs[2].getJavaDoc());
        assertEquals("deprecated-no-javadoc", attrs[3].getName());
        assertEquals("There is no other javadoc here.", attrs[3].getDeprecatedDoc());
        assertEquals("", attrs[3].getJavaDoc());
    }
    private Document _getDocument() throws Exception {
        Method method = AttrsXmlParser.class.getDeclaredMethod("getDocument"); 
        method.setAccessible(true);
        return (Document) method.invoke(mParser);
    }
}
