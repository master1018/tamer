@TestTargetClass(Node.class) 
public final class HCNodeDocumentFragmentNormalize extends DOMTestCase {
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies positive functionality of getNodeValue method, and that getNextSibling method returns null.",
            method = "getNodeValue",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies positive functionality of getNodeValue method, and that getNextSibling method returns null.",
            method = "getNextSibling",
            args = {}
        )
    })
    public void testNodeDocumentFragmentNormalize1() throws Throwable {
        Document doc;
        DocumentFragment docFragment;
        String nodeValue;
        Text txtNode;
        Node retval;
        doc = (Document) load("hc_staff", builder);
        docFragment = doc.createDocumentFragment();
        txtNode = doc.createTextNode("foo");
        retval = docFragment.appendChild(txtNode);
        txtNode = doc.createTextNode("bar");
        retval = docFragment.appendChild(txtNode);
        docFragment.normalize();
        txtNode = (Text) docFragment.getFirstChild();
        nodeValue = txtNode.getNodeValue();
        assertEquals("normalizedNodeValue", "foobar", nodeValue);
        retval = txtNode.getNextSibling();
        assertNull("singleChild", retval);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getFirstChild method returns null.",
        method = "getFirstChild",
        args = {}
    )
    public void testNodeDocumentFragmentNormalize2() throws Throwable {
        Document doc;
        DocumentFragment docFragment;
        Text txtNode;
        doc = (Document) load("hc_staff", builder);
        docFragment = doc.createDocumentFragment();
        txtNode = doc.createTextNode("");
        docFragment.appendChild(txtNode);
        docFragment.normalize();
        txtNode = (Text) docFragment.getFirstChild();
        assertNull("noChild", txtNode);
    }
}
