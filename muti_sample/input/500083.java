public final class nodegetnamespaceuri03 extends DOMTestCase {
   public nodegetnamespaceuri03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
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
      doc = (Document) load("staff", false);
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
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetnamespaceuri03.class, args);
   }
}
