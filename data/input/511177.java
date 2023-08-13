public final class getElementsByTagNameNS11 extends DOMTestCase {
   public getElementsByTagNameNS11(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element docElem;
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
      docElem = doc.getDocumentElement();
      elementList = docElem.getElementsByTagNameNS("*", "address");
      for (int indexN1005E = 0; indexN1005E < elementList.getLength(); indexN1005E++) {
          child = (Node) elementList.item(indexN1005E);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEquals("nodeNames", expectedResult, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS11.class, args);
   }
}
