@TestTargetClass(Node.class)
public final class NodeGetNamespaceURI extends DOMTestCase {
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
        method = "getNamespaceURI",
        args = {}
    )
   public void testGetNamespaceURI() throws Throwable {
      Document doc;
      Element element;
      Element elementNS;
      Attr attr;
      Attr attrNS;
      String elemNSURI;
      String elemNSURINull;
      String attrNSURI;
      String attrNSURINull;
      String nullNS = null;
      doc = (Document) load("staff", builder);
      element = doc.createElementNS(nullNS, "elem");
      elementNS = doc.createElementNS("http:
      attr = doc.createAttributeNS(nullNS, "attr");
      attrNS = doc.createAttributeNS("http:
      elemNSURI = elementNS.getNamespaceURI();
      elemNSURINull = element.getNamespaceURI();
      attrNSURI = attrNS.getNamespaceURI();
      attrNSURINull = attr.getNamespaceURI();
      assertEquals("nodegetnamespaceuri03_elemNSURI", "http:
      assertNull("nodegetnamespaceuri03_1", elemNSURINull);
      assertEquals("nodegetnamespaceuri03_attrNSURI", "http:
      assertNull("nodegetnamespaceuri03_2", attrNSURINull);
      }
}
