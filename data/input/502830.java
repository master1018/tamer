public final class getElementsByTagNameNS03 extends DOMTestCase {
   public getElementsByTagNameNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      expectedResult.add("employee");
      expectedResult.add("employeeId");
      expectedResult.add("name");
      expectedResult.add("position");
      expectedResult.add("salary");
      expectedResult.add("gender");
      expectedResult.add("address");
      expectedResult.add("emp:employee");
      expectedResult.add("emp:employeeId");
      expectedResult.add("emp:position");
      expectedResult.add("emp:salary");
      expectedResult.add("emp:gender");
      expectedResult.add("emp:address");
      expectedResult.add("address");
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("http:
      for (int indexN10076 = 0; indexN10076 < elementList.getLength(); indexN10076++) {
          child = (Node) elementList.item(indexN10076);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEquals("nodeNames", expectedResult, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS03.class, args);
   }
}
