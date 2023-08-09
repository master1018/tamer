@TestTargetClass(DOMImplementation.class) 
public final class CreateDocumentType extends DOMTestCase {
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
        notes = "Doesn't verify null as parameters.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType1() throws Throwable {
        String publicId = "STAFF";
        String systemId = "staff.xml";
        String malformedName = "prefix::local";
        Document doc;
        DOMImplementation domImpl;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        {
            boolean success = false;
            try {
                domImpl.createDocumentType(malformedName, publicId, systemId);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType2() throws Throwable {
        String publicId = "http:
        String systemId = "myDoc.dtd";
        String qualifiedName;
        Document doc;
        DOMImplementation domImpl;
        List<String> illegalQNames = new ArrayList<String>();
        illegalQNames.add("edi:{");
        illegalQNames.add("edi:}");
        illegalQNames.add("edi:~");
        illegalQNames.add("edi:'");
        illegalQNames.add("edi:!");
        illegalQNames.add("edi:@");
        illegalQNames.add("edi:#");
        illegalQNames.add("edi:$");
        illegalQNames.add("edi:%");
        illegalQNames.add("edi:^");
        illegalQNames.add("edi:&");
        illegalQNames.add("edi:*");
        illegalQNames.add("edi:(");
        illegalQNames.add("edi:)");
        illegalQNames.add("edi:+");
        illegalQNames.add("edi:=");
        illegalQNames.add("edi:[");
        illegalQNames.add("edi:]");
        illegalQNames.add("edi:\\");
        illegalQNames.add("edi:/");
        illegalQNames.add("edi:;");
        illegalQNames.add("edi:`");
        illegalQNames.add("edi:<");
        illegalQNames.add("edi:>");
        illegalQNames.add("edi:,");
        illegalQNames.add("edi:a ");
        illegalQNames.add("edi:\"");
        doc = (Document) load("staffNS", builder);
        for (int indexN1009A = 0; indexN1009A < illegalQNames.size(); indexN1009A++) {
            qualifiedName = (String) illegalQNames.get(indexN1009A);
            domImpl = doc.getImplementation();
            {
                boolean success = false;
                try {
                    domImpl.createDocumentType(qualifiedName, publicId,
                            systemId);
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
                }
                assertTrue("throw_INVALID_CHARACTER_ERR", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType3() throws Throwable {
        String qualifiedName = "prefix:myDoc";
        String publicId = "http:
        String systemId = "myDoc.dtd";
        Document doc;
        DOMImplementation domImpl;
        DocumentType newType = null;
        String nodeName;
        String nodeValue;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
        nodeName = newType.getNodeName();
        assertEquals("nodeName", "prefix:myDoc", nodeName);
        nodeValue = newType.getNodeValue();
        assertNull("nodeValue", nodeValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType4() throws Throwable {
        String publicId = "http:
        String systemId = "myDoc.dtd";
        DOMImplementation domImpl;
        domImpl = builder.getDOMImplementation();
        {
            boolean success = false;
            try {
                domImpl.createDocumentType("", publicId, systemId);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
}
