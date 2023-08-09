@TestTargetClass(Node.class) 
public final class NodeSetPrefix extends DOMTestCase {
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
        notes = "Doesn't verify DOMException.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix1() throws Throwable {
        Document doc;
        DocumentFragment docFragment;
        Element element;
        String elementTagName;
        String elementNodeName;
        doc = (Document) load("staff", builder);
        docFragment = doc.createDocumentFragment();
        element = doc.createElementNS("http:
                "emp:address");
        docFragment.appendChild(element);
        element.setPrefix("dmstc");
        elementTagName = element.getTagName();
        elementNodeName = element.getNodeName();
        assertEquals("nodesetprefix01_tagname", "dmstc:address", elementTagName);
        assertEquals("nodesetprefix01_nodeName", "dmstc:address",
                elementNodeName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix3() throws Throwable {
        Document doc;
        Element element;
        doc = (Document) load("staffNS", builder);
        element = doc.createElement("address");
        {
            boolean success = false;
            try {
                element.setPrefix("test");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix5() throws Throwable {
        Document doc;
        Element element;
        String prefixValue;
        List<String> prefixValues = new ArrayList<String>();
        prefixValues.add("_:");
        prefixValues.add(":0");
        prefixValues.add(":");
        prefixValues.add("_::");
        prefixValues.add("a:0:c");
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        for (int indexN10050 = 0; indexN10050 < prefixValues.size(); indexN10050++) {
            prefixValue = (String) prefixValues.get(indexN10050);
            {
                boolean success = false;
                try {
                    element.setPrefix(prefixValue);
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.NAMESPACE_ERR);
                }
                assertTrue("throw_NAMESPACE_ERR", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix6() throws Throwable {
        Document doc;
        Element element;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        {
            boolean success = false;
            try {
                element.setPrefix("xml");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix7() throws Throwable {
        Document doc;
        Attr attribute;
        doc = (Document) load("staffNS", builder);
        attribute = doc.createAttributeNS("http:
                "abc:elem");
        {
            boolean success = false;
            try {
                attribute.setPrefix("xmlns");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void testSetPrefix8() throws Throwable {
        Document doc;
        Element element;
        NodeList elementList;
        Attr attribute;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        element = (Element) elementList.item(0);
        attribute = element.getAttributeNode("xmlns");
        {
            boolean success = false;
            try {
                attribute.setPrefix("xml");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with INVALID_CHARACTER_ERR code.",
        method = "setPrefix",
        args = {java.lang.String.class}
    )
    public void _testSetPrefix9() throws Throwable {
        Document doc;
        String value = "#$%&'()@";
        Element element;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        {
            boolean success = false;
            try {
                element.setPrefix(value);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
            }
            assertTrue("throw_INVALID_CHARACTER_ERR", success);
        }
    }
}
