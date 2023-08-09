@TestTargetClass(DOMImplementation.class) 
public final class DOMImplementationHasFeature extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration1());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasFeature returns true value.",
        method = "hasFeature",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasFeatureCore() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        boolean state;
        doc = (Document) load("staff", builder);
        domImpl = doc.getImplementation();
        state = domImpl.hasFeature("core", "2.0");
        assertTrue("domimplementationFeaturecoreAssert", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasFeature returns true value.",
        method = "hasFeature",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasFeatureXml() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        boolean state;
        doc = (Document) load("staff", builder);
        domImpl = doc.getImplementation();
        state = domImpl.hasFeature("xml", "2.0");
        assertTrue("domimplementationFeaturexmlVersion2Assert", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify that hasFeature method returns false.",
        method = "hasFeature",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasFeature1() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        String version = "";
        String version1 = "1.0";
        String version2 = "2.0";
        String featureCore;
        String featureXML;
        boolean success;
        List<String> featuresXML = new ArrayList<String>();
        featuresXML.add("XML");
        featuresXML.add("xmL");
        List<String> featuresCore = new ArrayList<String>();
        featuresCore.add("Core");
        featuresCore.add("CORE");
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        for (int indexN10063 = 0; indexN10063 < featuresXML.size(); indexN10063++) {
            featureXML = (String) featuresXML.get(indexN10063);
            success = domImpl.hasFeature(featureXML, version);
            assertTrue("domimplementationhasfeature01_XML_1", success);
            success = domImpl.hasFeature(featureXML, version1);
            assertTrue("domimplementationhasfeature01_XML_2", success);
        }
        for (int indexN1007C = 0; indexN1007C < featuresCore.size(); indexN1007C++) {
            featureCore = (String) featuresCore.get(indexN1007C);
            success = domImpl.hasFeature(featureCore, version);
            assertTrue("domimplementationhasfeature01_Core_1", success);
            success = domImpl.hasFeature(featureCore, version1);
            success = domImpl.hasFeature(featureCore, version2);
            assertTrue("domimplementationhasfeature01_Core_3", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasFeature method returns false.",
        method = "hasFeature",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasFeature2() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        boolean success;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        success = domImpl.hasFeature("Blah Blah", "");
        assertFalse("domimplementationhasfeature02", success);
    }
}
