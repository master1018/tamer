@TestTargetClass(Node.class) 
public final class NodeGetLocalName extends DOMTestCase {
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
        notes = "Doesn't verify that getLocalName method returns null.",
        method = "getLocalName",
        args = {}
    )
   public void testGetLocalName() throws Throwable {
      Document doc;
      Element element;
      Element qelement;
      Attr attr;
      Attr qattr;
      String localElemName;
      String localQElemName;
      String localAttrName;
      String localQAttrName;
      doc = (Document) load("staff", builder);
      element = doc.createElementNS("http:
      qelement = doc.createElementNS("http:
      attr = doc.createAttributeNS("http:
      qattr = doc.createAttributeNS("http:
      localElemName = element.getLocalName();
      localQElemName = qelement.getLocalName();
      localAttrName = attr.getLocalName();
      localQAttrName = qattr.getLocalName();
      assertEquals("nodegetlocalname03_localElemName", "elem", localElemName);
      assertEquals("nodegetlocalname03_localQElemName", "qelem", localQElemName);
      assertEquals("nodegetlocalname03_localAttrName", "attr", localAttrName);
      assertEquals("nodegetlocalname03_localQAttrName", "qattr", localQAttrName);
      }
}
