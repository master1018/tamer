public final class nodegetprefix03 extends DOMTestCase {
   public nodegetprefix03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element qelement;
      Attr attr;
      Attr qattr;
      String elemNoPrefix;
      String elemPrefix;
      String attrNoPrefix;
      String attrPrefix;
      doc = (Document) load("staff", false);
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
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetprefix03.class, args);
   }
}
