@TestTargetClass(SAXParserFactory.class) 
public class SAXParserFactoryTest extends TestCase {
    SAXParserFactory spf;
    InputStream is1;
    static HashMap<String, String> ns;
    static Vector<String> el;
    static HashMap<String, String> attr;
    public void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        is1 = getClass().getResourceAsStream("/simple.xml");
        ns = new HashMap<String, String>();
        attr = new HashMap<String, String>();
        el = new Vector<String>();
    }
    public void tearDown() throws Exception {
        TestEnvironment.reset();
        is1.close();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SAXParserFactory",
        args = {}
    )
    @AndroidOnly("Android SAX implementation is non-validating")
    public void test_Constructor() {
        MySAXParserFactory mpf = new MySAXParserFactory();
        assertTrue(mpf instanceof SAXParserFactory);
        assertFalse(mpf.isValidating());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isNamespaceAware",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setNamespaceAware",
            args = {boolean.class}
        )
    })
    public void test_setIsNamespaceAware() {
        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
        spf.setNamespaceAware(false);
        assertFalse(spf.isNamespaceAware());
        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isValidating",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "setValidating",
            args = {boolean.class}
        )
    })
    public void test_setIsValidating() {
        spf.setValidating(true);
        assertTrue(spf.isValidating());
        spf.setValidating(false);
        assertFalse(spf.isValidating());
        spf.setValidating(true);
        assertTrue(spf.isValidating());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isXIncludeAware",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "setXIncludeAware",
            args = {boolean.class}
        )
    })
    @KnownFailure("Should handle XIncludeAware flag more gracefully")
    public void test_setIsXIncludeAware() {
        spf.setXIncludeAware(true);
        assertTrue(spf.isXIncludeAware());
        spf.setXIncludeAware(false);
        assertFalse(spf.isXIncludeAware());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newInstance",
        args = {}
    )
    @KnownFailure("Dalvik doesn't honor system properties when choosing a SAX implementation")
    public void test_newInstance() {
        try {
            SAXParserFactory dtf = SAXParserFactory.newInstance();
            assertNotNull("New Instance of DatatypeFactory is null", dtf);
            System.setProperty("javax.xml.parsers.SAXParserFactory",
            "org.apache.harmony.xml.parsers.SAXParserFactoryImpl");
            SAXParserFactory spf1 = SAXParserFactory.newInstance();
            assertTrue(spf1 instanceof org.apache.harmony.xml.parsers.SAXParserFactoryImpl);
            String key = "javax.xml.parsers.SAXParserFactory = org.apache.harmony.xml.parsers.SAXParserFactoryImpl";
            ByteArrayInputStream bis = new ByteArrayInputStream(key.getBytes());
            Properties prop = System.getProperties();
            prop.load(bis);
            SAXParserFactory spf2 = SAXParserFactory.newInstance();
            assertTrue(spf2 instanceof org.apache.harmony.xml.parsers.SAXParserFactoryImpl);
            System.setProperty("javax.xml.parsers.SAXParserFactory", "");
            try {
                SAXParserFactory.newInstance();
                fail("Expected FactoryConfigurationError was not thrown");
            } catch (FactoryConfigurationError e) {
            }
        } catch (IOException ioe) {
            fail("Unexpected exception " + ioe.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SAXException untested; unused on Android",
        method = "newSAXParser",
        args = {}
    )
    public void test_newSAXParser() {
        try {
            SAXParser sp = spf.newSAXParser();
            assertTrue(sp instanceof SAXParser);
            sp.parse(is1, new MyHandler());
        } catch(Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        spf.setValidating(true);
        try {
            SAXParser sp = spf.newSAXParser();
        } catch(ParserConfigurationException e) {
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setFeature",
            notes = "ParserConfigurationException untested; unused on Android",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getFeature",
            notes = "ParserConfigurationException untested; unused on Android",
            args = {java.lang.String.class}
        )
    })        
    public void test_setFeatureLjava_lang_StringZ() {
        String[] features = {
                "http:
                "http:
        for (int i = 0; i < features.length; i++) {
            try {
                spf.setFeature(features[i], true);
                assertTrue(spf.getFeature(features[i]));
                spf.setFeature(features[i], false);
                assertFalse(spf.getFeature(features[i]));
            } catch (ParserConfigurationException pce) {
                fail("ParserConfigurationException is thrown");
            } catch (SAXNotRecognizedException snre) {
                fail("SAXNotRecognizedException is thrown");
            } catch (SAXNotSupportedException snse) {
                fail("SAXNotSupportedException is thrown");
            }
        }
        try {
            spf.setFeature("", true);
            fail("SAXNotRecognizedException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
            fail("NullPointerException is thrown");
        }
        try {
            spf.setFeature("http:
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            fail("SAXNotRecognizedException is thrown");
        } catch (SAXNotSupportedException snse) {
        } catch (NullPointerException npe) {
            fail("NullPointerException is thrown");
        }
        try {
            spf.setFeature(null, true);
            fail("NullPointerException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            fail("SAXNotRecognizedException is thrown");
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setNamespaceAware",
        args = {boolean.class}
    )
    public void test_setNamespaceAwareZ() throws Exception {
        MyHandler mh = new MyHandler();
        spf.setNamespaceAware(true);
        InputStream is = getClass().getResourceAsStream("/simple_ns.xml");
        spf.newSAXParser().parse(is, mh);
        is.close();
        spf.setNamespaceAware(false);
        is = getClass().getResourceAsStream("/simple_ns.xml");
        spf.newSAXParser().parse(is, mh);
        is.close();
    }
    static class MyHandler extends DefaultHandler {
        public void startElement(String uri, String localName, String qName,
                Attributes atts) {
            el.add(qName);
            if (!uri.equals(""))
                ns.put(qName, uri);
            for (int i = 0; i < atts.getLength(); i++) {
                attr.put(atts.getQName(i), atts.getValue(i));
            }
        }
    }
    class MySAXParserFactory extends SAXParserFactory {
        public MySAXParserFactory() {
            super();
        }
        public SAXParser newSAXParser() {
            return null;
        }
        public void setFeature(String name, boolean value) throws
                ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException {
        }
        public boolean getFeature(String name) throws 
                ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException {
            return true;
        }
    }
}
