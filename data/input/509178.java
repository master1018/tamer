@TestTargetClass(Document.class) 
public final class CreateAttributeNS extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration2());
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
        notes = "Verifies NAMESPACE_ERR exception code.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS1() throws Throwable {
        String namespaceURI = "http:
        String malformedName = "prefix::local";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createAttributeNS(namespaceURI, malformedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies createAttributeNS method with null as the fisrt parameter.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS2() throws Throwable {
        String namespaceURI = null;
        String qualifiedName = "prefix:local";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createAttributeNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS3() throws Throwable {
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
        for (int indexN10090 = 0; indexN10090 < illegalQNames.size(); indexN10090++) {
            qualifiedName = (String) illegalQNames.get(indexN10090);
            {
                boolean success = false;
                try {
                    doc.createAttributeNS(namespaceURI, qualifiedName);
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
                }
                assertTrue("throw_INVALID_CHARACTER_ERR", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS4() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "xml:attr1";
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createAttributeNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS5() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "econm:local";
        Document doc;
        Attr newAttr;
        String attrName;
        doc = (Document) load("staffNS", builder);
        newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
        attrName = newAttr.getName();
        assertEquals("throw_Equals", qualifiedName, attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS6() throws Throwable {
        String namespaceURI = "http:
        Document doc;
        doc = (Document) load("hc_staff", builder);
        try {
            doc.createAttributeNS(namespaceURI, "");
            fail();
        } catch (DOMException ex) {
        }
    }
}
