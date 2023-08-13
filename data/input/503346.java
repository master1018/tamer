@TestTargetClass(Document.class) 
public final class ImportNode extends DOMTestCase {
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
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void _testImportNode1() throws Throwable {
        Document doc;
        Document aNewDoc;
        Attr newAttr;
        Text importedChild;
        Node aNode;
        Document ownerDocument;
        Element attrOwnerElement;
        DocumentType docType;
        String system;
        boolean specified;
        NodeList childList;
        String nodeName;
        Node child;
        String childValue;
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("elem:attr1");
        expectedResult.add("importedText");
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        newAttr = aNewDoc.createAttribute("elem:attr1");
        importedChild = aNewDoc.createTextNode("importedText");
        aNode = newAttr.appendChild(importedChild);
        aNode = doc.importNode(newAttr, false);
        ownerDocument = aNode.getOwnerDocument();
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertNotNull("aNode", aNode);
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        attrOwnerElement = ((Attr) aNode).getOwnerElement();
        assertNull("ownerElement", attrOwnerElement);
        specified = ((Attr) aNode).getSpecified();
        assertTrue("specified", specified);
        childList = aNode.getChildNodes();
        assertEquals("childList", 1, childList.getLength());
        nodeName = aNode.getNodeName();
        assertEquals("nodeName", "elem:attr1", nodeName);
        child = aNode.getFirstChild();
        childValue = child.getNodeValue();
        assertEquals("childValue", "importedText", childValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode2() throws Throwable {
        Document doc;
        Document aNewDoc;
        CDATASection cDataSec;
        Node aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String value;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        cDataSec = aNewDoc.createCDATASection("this is CDATASection data");
        aNode = doc.importNode(cDataSec, false);
        ownerDocument = aNode.getOwnerDocument();
        assertNotNull("ownerDocumentNotNull", ownerDocument);
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("dtdSystemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        value = aNode.getNodeValue();
        assertEquals("nodeValue", "this is CDATASection data", value);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode3() throws Throwable {
        Document doc;
        Document aNewDoc;
        Comment comment;
        Node aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String value;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        comment = aNewDoc.createComment("this is a comment");
        aNode = doc.importNode(comment, false);
        ownerDocument = aNode.getOwnerDocument();
        assertNotNull("ownerDocumentNotNull", ownerDocument);
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        value = aNode.getNodeValue();
        assertEquals("nodeValue", "this is a comment", value);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode4() throws Throwable {
        Document doc;
        Document aNewDoc;
        DocumentFragment docFrag;
        Comment comment;
        Node aNode;
        NodeList children;
        Node child;
        String childValue;
        doc = (Document) load("staff", builder);
        aNewDoc = (Document) load("staff", builder);
        docFrag = aNewDoc.createDocumentFragment();
        comment = aNewDoc.createComment("descendant1");
        aNode = docFrag.appendChild(comment);
        aNode = doc.importNode(docFrag, true);
        children = aNode.getChildNodes();
        assertEquals("throw_Size", 1, children.getLength());
        child = aNode.getFirstChild();
        childValue = child.getNodeValue();
        assertEquals("descendant1", "descendant1", childValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode5() throws Throwable {
        Document doc;
        Document aNewDoc;
        Element element;
        Node aNode;
        boolean hasChild;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String name;
        NodeList addresses;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        addresses = aNewDoc.getElementsByTagName("emp:address");
        element = (Element) addresses.item(0);
        assertNotNull("empAddressNotNull", element);
        aNode = doc.importNode(element, false);
        hasChild = aNode.hasChildNodes();
        assertFalse("hasChild", hasChild);
        ownerDocument = aNode.getOwnerDocument();
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("dtdSystemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        name = aNode.getNodeName();
        assertEquals("nodeName", "emp:address", name);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode6() throws Throwable {
        Document doc;
        Document aNewDoc;
        Element element;
        Node aNode;
        boolean hasChild;
        String name;
        Node child;
        String value;
        NodeList addresses;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        addresses = aNewDoc.getElementsByTagName("emp:address");
        element = (Element) addresses.item(0);
        assertNotNull("empAddressNotNull", element);
        aNode = doc.importNode(element, true);
        hasChild = aNode.hasChildNodes();
        assertTrue("throw_True", hasChild);
        name = aNode.getNodeName();
        assertEquals("nodeName", "emp:address", name);
        child = aNode.getFirstChild();
        value = child.getNodeValue();
        assertEquals("nodeValue", "27 South Road. Dallas, texas 98556", value);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode8() throws Throwable {
        Document doc;
        Document aNewDoc;
        DocumentFragment docFrag;
        Node aNode;
        boolean hasChild;
        Document ownerDocument;
        DocumentType docType;
        String system;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        docFrag = aNewDoc.createDocumentFragment();
        aNode = doc.importNode(docFrag, false);
        hasChild = aNode.hasChildNodes();
        assertFalse("hasChild", hasChild);
        ownerDocument = aNode.getOwnerDocument();
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("system", null, null, null, "staffNS.dtd", null, null,
                null, null, system);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode10() throws Throwable {
        Document doc;
        Document aNewDoc;
        EntityReference entRef;
        Node aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String name;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        entRef = aNewDoc.createEntityReference("entRef1");
        assertNotNull("createdEntRefNotNull", entRef);
        entRef.setNodeValue("entRef1Value");
        aNode = doc.importNode(entRef, false);
        ownerDocument = aNode.getOwnerDocument();
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        name = aNode.getNodeName();
        assertEquals("nodeName", "entRef1", name);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode14() throws Throwable {
        Document doc;
        Document aNewDoc;
        ProcessingInstruction pi;
        ProcessingInstruction aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String target;
        String data;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        pi = aNewDoc.createProcessingInstruction("target1", "data1");
        aNode = (ProcessingInstruction) doc.importNode(pi, false);
        ownerDocument = aNode.getOwnerDocument();
        assertNotNull("ownerDocumentNotNull", ownerDocument);
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        target = aNode.getTarget();
        assertEquals("piTarget", "target1", target);
        data = aNode.getData();
        assertEquals("piData", "data1", data);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode15() throws Throwable {
        Document doc;
        Document aNewDoc;
        Text text;
        Node aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String value;
        doc = (Document) load("staffNS", builder);
        aNewDoc = (Document) load("staffNS", builder);
        text = aNewDoc.createTextNode("this is text data");
        aNode = doc.importNode(text, false);
        ownerDocument = aNode.getOwnerDocument();
        assertNotNull("ownerDocumentNotNull", ownerDocument);
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, system);
        value = aNode.getNodeValue();
        assertEquals("nodeValue", "this is text data", value);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that importNode method throws DOMException with NOT_SUPPORTED_ERR code.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode16() throws Throwable {
        Document doc;
        Document anotherDoc;
        DocumentType docType;
        doc = (Document) load("staffNS", builder);
        anotherDoc = (Document) load("staffNS", builder);
        docType = anotherDoc.getDoctype();
        {
            boolean success = false;
            try {
                doc.importNode(docType, false);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
            }
            assertTrue("throw_NOT_SUPPORTED_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that importNode method throws DOMException with NOT_SUPPORTED_ERR code.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode17() throws Throwable {
        Document doc;
        Document anotherDoc;
        doc = (Document) load("staffNS", builder);
        anotherDoc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.importNode(anotherDoc, false);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
            }
            assertTrue("throw_NOT_SUPPORTED_ERR", success);
        }
    }
}
