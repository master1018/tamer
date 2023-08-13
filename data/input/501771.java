@TestTargetClass(Document.class) 
public final class DocumentCreateAttributeNS extends DOMTestCase {
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
        notes = "Verifies positive functionality.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS1() throws Throwable {
        Document doc;
        Attr attribute;
        String namespaceURI = null;
        String qualifiedName = "test";
        String nodeName;
        doc = (Document) load("staffNS", builder);
        attribute = doc.createAttributeNS(namespaceURI, qualifiedName);
        nodeName = attribute.getNodeName();
        assertEquals("documentcreateattributeNS01", "test", nodeName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS2() throws Throwable {
        Document doc;
        Attr attribute1;
        Attr attribute2;
        String name;
        String nodeName;
        String nodeValue;
        String prefix;
        String namespaceURI;
        doc = (Document) load("staffNS", builder);
        attribute1 = doc.createAttributeNS(
                "http:
        name = attribute1.getName();
        nodeName = attribute1.getNodeName();
        nodeValue = attribute1.getNodeValue();
        prefix = attribute1.getPrefix();
        namespaceURI = attribute1.getNamespaceURI();
        assertEquals("documentcreateattributeNS02_att1_name", "xml:xml", name);
        assertEquals("documentcreateattributeNS02_att1_nodeName", "xml:xml",
                nodeName);
        assertEquals("documentcreateattributeNS02_att1_nodeValue", "",
                nodeValue);
        assertEquals("documentcreateattributeNS02_att1_prefix", "xml", prefix);
        assertEquals("documentcreateattributeNS02_att1_namespaceURI",
                "http:
        attribute2 = doc.createAttributeNS("http:
                "xmlns");
        name = attribute2.getName();
        nodeName = attribute2.getNodeName();
        nodeValue = attribute2.getNodeValue();
        prefix = attribute2.getPrefix();
        namespaceURI = attribute2.getNamespaceURI();
        assertEquals("documentcreateattributeNS02_att2_name", "xmlns", name);
        assertEquals("documentcreateattributeNS02_att2_nodeName", "xmlns",
                nodeName);
        assertEquals("documentcreateattributeNS02_att2_nodeValue", "",
                nodeValue);
        assertEquals("documentcreateattributeNS02_att2_namespaceURI",
                "http:
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException with INVALID_CHARACTER_ERR code.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS3() throws Throwable {
        Document doc;
        String namespaceURI = "http:
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("/");
        qualifiedNames.add("
        qualifiedNames.add("\\");
        qualifiedNames.add(";");
        qualifiedNames.add("&");
        qualifiedNames.add("*");
        qualifiedNames.add("]]");
        qualifiedNames.add(">");
        qualifiedNames.add("<");
        doc = (Document) load("staffNS", builder);
        for (int indexN1005A = 0; indexN1005A < qualifiedNames.size(); indexN1005A++) {
            qualifiedName = (String) qualifiedNames.get(indexN1005A);
            {
                boolean success = false;
                try {
                    doc.createAttributeNS(namespaceURI, qualifiedName);
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
                }
                assertTrue("documentcreateattributeNS03", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException with NAMESPACE_ERR code.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS4() throws Throwable {
        Document doc;
        String namespaceURI = "http:
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("_:");
        qualifiedNames.add(":0a");
        qualifiedNames.add(":");
        qualifiedNames.add("a:b:c");
        qualifiedNames.add("_::a");
        doc = (Document) load("staffNS", builder);
        for (int indexN1004E = 0; indexN1004E < qualifiedNames.size(); indexN1004E++) {
            qualifiedName = (String) qualifiedNames.get(indexN1004E);
            {
                try {
                    doc.createAttributeNS(namespaceURI, qualifiedName);
                    fail();
                } catch (DOMException ex) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException with NAMESPACE_ERR code.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS5() throws Throwable {
        Document doc;
        Document newDoc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        String namespaceURI = null;
        String qualifiedName = "abc:def";
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument("http:
                "dom:doc", docType);
        {
            boolean success = false;
            try {
                newDoc.createAttributeNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("documentcreateattributeNS05", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException with NAMESPACE_ERR code.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS6() throws Throwable {
        Document doc;
        Document newDoc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        String namespaceURI = "http:
        String qualifiedName = "xml:root";
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument("http:
                "dom:doc", docType);
        {
            boolean success = false;
            try {
                newDoc.createAttributeNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("documentcreateattributeNS06", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createAttributeNS throws DOMException with NAMESPACE_ERR code.",
        method = "createAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateAttributeNS7() throws Throwable {
        Document doc;
        String namespaceURI = "http:
        String qualifiedName = "xmlns";
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createAttributeNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("documentcreateattributeNS07", success);
        }
    }
}
