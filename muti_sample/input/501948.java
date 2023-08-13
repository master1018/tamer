@TestTargetClass(XMLReaderFactory.class)
public class XMLReaderFactoryTest extends TestCase {
    @Override protected void setUp() throws Exception {
        TestEnvironment.reset();
        super.setUp();
    }
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "createXMLReader",
        args = { },
        notes = "Checks everything except META-INF case"
    )
    public void testCreateXMLReader() {
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
        }
        System.setProperty("org.xml.sax.driver", "foo.bar.XMLReader");
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
        }
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoAccessXMLReader");
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
        }
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoInstanceXMLReader");
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
        }
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoSubclassXMLReader");
        try {
            XMLReaderFactory.createXMLReader();
        } catch (ClassCastException e) {
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.DoNothingXMLReader");
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "createXMLReader",
        args = { String.class }
    )
    public void testMakeParserString() {
        try {
            XMLReaderFactory.createXMLReader(null);
        } catch (NullPointerException e) {
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            XMLReaderFactory.createXMLReader("foo.bar.XMLReader");
        } catch (SAXException e) {
        }
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoAccessXMLReader");
        } catch (SAXException e) {
        }
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoInstanceXMLReader");
        } catch (SAXException e) {
        }
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoSubclassXMLReader");
        } catch (SAXException e) {
        }
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.DoNothingXMLReader");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
