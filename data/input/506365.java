@TestTargetClass(Node.class) 
public final class NodeGetPrefix extends DOMTestCase {
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
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrefix",
        args = {}
    )
   public void testGetPrefix() throws Throwable {
      Document doc;
      Element element;
      Element qelement;
      Attr attr;
      Attr qattr;
      String elemNoPrefix;
      String elemPrefix;
      String attrNoPrefix;
      String attrPrefix;
      doc = (Document) load("staff", builder);
      element = doc.createElementNS("http:
      qelement = doc.createElementNS("http:
      attr = doc.createAttributeNS("http:
      qattr = doc.createAttributeNS("http:
      elemNoPrefix = element.getPrefix();
      elemPrefix = qelement.getPrefix();
      attrNoPrefix = attr.getPrefix();
      attrPrefix = qattr.getPrefix();
      assertNull("nodegetprefix03_1", elemNoPrefix);
      assertEquals("nodegetprefix03_2", "qual", elemPrefix);
      assertNull("nodegetprefix03_3", attrNoPrefix);
      assertEquals("nodegetprefix03_4", "qual", attrPrefix);
      }
}
