public final class nodegetlocalname03 extends DOMTestCase {
   public nodegetlocalname03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String localElemName;
      String localQElemName;
      String localAttrName;
      String localQAttrName;
      doc = (Document) load("staff", false);
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
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetlocalname03.class, args);
   }
}
