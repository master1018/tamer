@TestTargetClass(Node.class) 
public final class HasAttributes extends DOMTestCase {
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
        notes = "Verifies that hasAttributes method returns false value.",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes1() throws Throwable {
        Document doc;
        NodeList addrList;
        Node addrNode;
        boolean state;
        doc = (Document) load("staff", builder);
        addrList = doc.getElementsByTagName("name");
        addrNode = addrList.item(0);
        state = addrNode.hasAttributes();
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that hasAttributes method returns true value.",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes2() throws Throwable {
        Document doc;
        NodeList addrList;
        Node addrNode;
        boolean state;
        doc = (Document) load("staff", builder);
        addrList = doc.getElementsByTagName("address");
        addrNode = addrList.item(0);
        state = addrNode.hasAttributes();
        assertTrue("throw_True", state);
    }
}
