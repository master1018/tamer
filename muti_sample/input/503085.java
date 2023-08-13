@TestTargetClass(Document.class) 
public final class DocumentImportNode extends DOMTestCase {
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
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode2() throws Throwable {
        Document doc;
        Document docImported;
        Element element;
        Attr attr;
        Node importedAttr;
        String nodeName;
        int nodeType;
        String nodeValue;
        NodeList addresses;
        Node attrsParent;
        doc = (Document) load("staffNS", builder);
        docImported = (Document) load("staff", builder);
        addresses = doc
                .getElementsByTagNameNS("http:
        element = (Element) addresses.item(1);
        attr = element.getAttributeNodeNS("http:
        importedAttr = docImported.importNode(attr, false);
        nodeName = importedAttr.getNodeName();
        nodeType = (int) importedAttr.getNodeType();
        nodeValue = importedAttr.getNodeValue();
        attrsParent = importedAttr.getParentNode();
        assertNull("documentimportnode02_parentNull", attrsParent);
        assertEquals("documentimportnode02_nodeName", "emp:zone", nodeName);
        assertEquals("documentimportnode02_nodeType", 2, nodeType);
        assertEquals("documentimportnode02_nodeValue", "CANADA", nodeValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode5() throws Throwable {
        Document doc;
        Document docImported;
        Attr attr;
        Node importedAttr;
        String nodeName;
        int nodeType;
        String nodeValue;
        String namespaceURI;
        doc = (Document) load("staffNS", builder);
        docImported = (Document) load("staff", builder);
        attr = doc.createAttributeNS("http:
        importedAttr = docImported.importNode(attr, false);
        nodeName = importedAttr.getNodeName();
        nodeValue = importedAttr.getNodeValue();
        nodeType = (int) importedAttr.getNodeType();
        namespaceURI = importedAttr.getNamespaceURI();
        assertEquals("documentimportnode05_nodeName", "a_:b0", nodeName);
        assertEquals("documentimportnode05_nodeType", 2, nodeType);
        assertEquals("documentimportnode05_nodeValue", "", nodeValue);
        assertEquals("documentimportnode05_namespaceURI",
                "http:
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that importNode method throws DOMException with NOT_SUPPORTED_ERR code.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode6() throws Throwable {
        Document doc;
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.importNode(doc, false);
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
    public void testImportNode7() throws Throwable {
        Document doc;
        DocumentType docType;
        doc = (Document) load("staffNS", builder);
        docType = doc.getDoctype();
        {
            boolean success = false;
            try {
                doc.importNode(docType, true);
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
    public void testImportNode8() throws Throwable {
        Document doc;
        DocumentType docType;
        DOMImplementation domImpl;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        docType = domImpl.createDocumentType("test:root", nullNS, nullNS);
        {
            boolean success = false;
            try {
                doc.importNode(docType, true);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
            }
            assertTrue("throw_NOT_SUPPORTED_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode9() throws Throwable {
        Document doc;
        DocumentFragment docFragment;
        NodeList childList;
        boolean success;
        Node addressNode;
        Node importedDocFrag;
        doc = (Document) load("staffNS", builder);
        docFragment = doc.createDocumentFragment();
        childList = doc.getElementsByTagNameNS("*", "address");
        addressNode = childList.item(0);
        docFragment.appendChild(addressNode);
        importedDocFrag = doc.importNode(docFragment, false);
        success = importedDocFrag.hasChildNodes();
        assertFalse("documentimportnode09", success);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality; doesn't verify DOMException exceptions.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode10() throws Throwable {
        Document doc;
        DocumentFragment docFragment;
        NodeList childList;
        boolean success;
        Node addressNode;
        Node importedDocFrag;
        doc = (Document) load("staffNS", builder);
        docFragment = doc.createDocumentFragment();
        childList = doc.getElementsByTagNameNS("*", "address");
        addressNode = childList.item(0);
        docFragment.appendChild(addressNode);
        importedDocFrag = doc.importNode(docFragment, true);
        success = importedDocFrag.hasChildNodes();
        assertTrue("documentimportnode10", success);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode11() throws Throwable {
        Document doc;
        Element docElement;
        Node imported;
        boolean success;
        String nodeNameOrig;
        String nodeNameImported;
        doc = (Document) load("staffNS", builder);
        docElement = doc.getDocumentElement();
        imported = doc.importNode(docElement, false);
        success = imported.hasChildNodes();
        assertFalse("documentimportnode11", success);
        nodeNameImported = imported.getNodeName();
        nodeNameOrig = docElement.getNodeName();
        assertEquals("documentimportnode11_NodeName", nodeNameImported,
                nodeNameOrig);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode12() throws Throwable {
        Document doc;
        NodeList childList;
        Node imported;
        Node addressElem;
        NodeList addressElemChildren;
        NodeList importedChildren;
        int addressElemLen;
        int importedLen;
        doc = (Document) load("staffNS", builder);
        childList = doc.getElementsByTagNameNS("*", "address");
        addressElem = childList.item(0);
        imported = doc.importNode(addressElem, true);
        addressElemChildren = addressElem.getChildNodes();
        importedChildren = imported.getChildNodes();
        addressElemLen = (int) addressElemChildren.getLength();
        importedLen = (int) importedChildren.getLength();
        assertEquals("documentimportnode12", importedLen, addressElemLen);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode13() throws Throwable {
        Document doc;
        NodeList childList;
        Node imported;
        NodeList importedList;
        Node employeeElem;
        int importedLen;
        doc = (Document) load("staffNS", builder);
        childList = doc.getElementsByTagNameNS("*", "employee");
        employeeElem = childList.item(0);
        imported = doc.importNode(employeeElem, false);
        importedList = imported.getChildNodes();
        importedLen = (int) importedList.getLength();
        assertEquals("documentimportnode13", 0, importedLen);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies import of TEXT_NODE.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode15() throws Throwable {
        Document doc;
        Node textImport;
        Node textToImport;
        String nodeValue;
        doc = (Document) load("staffNS", builder);
        textToImport = doc
                .createTextNode("Document.importNode test for a TEXT_NODE");
        textImport = doc.importNode(textToImport, true);
        nodeValue = textImport.getNodeValue();
        assertEquals("documentimportnode15",
                "Document.importNode test for a TEXT_NODE", nodeValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies import of COMMENT_NODE",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode17() throws Throwable {
        Document doc;
        Node commentImport;
        Node commentToImport;
        String nodeValue;
        doc = (Document) load("staffNS", builder);
        commentToImport = doc
                .createComment("Document.importNode test for a COMMENT_NODE");
        commentImport = doc.importNode(commentToImport, true);
        nodeValue = commentImport.getNodeValue();
        assertEquals("documentimportnode17",
                "Document.importNode test for a COMMENT_NODE", nodeValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException exception.",
        method = "importNode",
        args = {org.w3c.dom.Node.class, boolean.class}
    )
    public void testImportNode18() throws Throwable {
        Document doc;
        ProcessingInstruction piImport;
        ProcessingInstruction piToImport;
        String piData;
        String piTarget;
        doc = (Document) load("staffNS", builder);
        piToImport = doc.createProcessingInstruction("Target", "Data");
        piImport = (ProcessingInstruction) doc.importNode(piToImport, false);
        piTarget = piImport.getTarget();
        piData = piImport.getData();
        assertEquals("documentimportnode18_Target", "Target", piTarget);
        assertEquals("documentimportnode18_Data", "Data", piData);
    }
}
