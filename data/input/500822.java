@TestTargetClass(Document.class) 
public final class CreateElementNS extends DOMTestCase {
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
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS1() throws Throwable {
        String namespaceURI = "http:
        String malformedName = "prefix::local";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createElementNS(namespaceURI, malformedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS2() throws Throwable {
        String namespaceURI = null;
        String qualifiedName = "prefix:local";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createElementNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS3() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName;
        Document doc;
        List<String> illegalQNames = new ArrayList<String>();
        illegalQNames.add("person:{");
        illegalQNames.add("person:}");
        illegalQNames.add("person:~");
        illegalQNames.add("person:'");
        illegalQNames.add("person:!");
        illegalQNames.add("person:@");
        illegalQNames.add("person:#");
        illegalQNames.add("person:$");
        illegalQNames.add("person:%");
        illegalQNames.add("person:^");
        illegalQNames.add("person:&");
        illegalQNames.add("person:*");
        illegalQNames.add("person:(");
        illegalQNames.add("person:)");
        illegalQNames.add("person:+");
        illegalQNames.add("person:=");
        illegalQNames.add("person:[");
        illegalQNames.add("person:]");
        illegalQNames.add("person:\\");
        illegalQNames.add("person:/");
        illegalQNames.add("person:;");
        illegalQNames.add("person:`");
        illegalQNames.add("person:<");
        illegalQNames.add("person:>");
        illegalQNames.add("person:,");
        illegalQNames.add("person:a ");
        illegalQNames.add("person:\"");
        doc = (Document) load("staffNS", builder);
        for (int indexN10098 = 0; indexN10098 < illegalQNames.size(); indexN10098++) {
            qualifiedName = (String) illegalQNames.get(indexN10098);
            {
                boolean success = false;
                try {
                    doc.createElementNS(namespaceURI, qualifiedName);
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
                }
                assertTrue("throw_INVALID_CHARACTER_ERR", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS4() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "xml:element1";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createElementNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS5() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "gov:faculty";
        Document doc;
        Element newElement;
        String elementName;
        doc = (Document) load("staffNS", builder);
        newElement = doc.createElementNS(namespaceURI, qualifiedName);
        elementName = newElement.getTagName();
        assertEquals("throw_Equals", qualifiedName, elementName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameters, and other types of DOMException.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS6() throws Throwable {
        String namespaceURI = "http:
        Document doc;
        doc = (Document) load("hc_staff", builder);
        {
            try {
                doc.createElementNS(namespaceURI, "");
                fail();
            } catch (DOMException ex) {
            }
        }
    }
}
