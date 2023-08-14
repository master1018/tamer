public final class getElementsByTagNameNS04 extends DOMTestCase {
   public getElementsByTagNameNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node child;
      String childName;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedResult = new java.util.ArrayList();
      expectedResult.add("address");
      expectedResult.add("address");
      expectedResult.add("address");
      expectedResult.add("emp:address");
      expectedResult.add("address");
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("*", "address");
      for (int indexN10059 = 0; indexN10059 < elementList.getLength(); indexN10059++) {
          child = (Node) elementList.item(indexN10059);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEquals("nodeNames", expectedResult, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS04.class, args);
   }
}
