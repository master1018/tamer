public final class elementgetelementsbytagnamespecialvalue extends DOMTestCase {
   public elementgetelementsbytagnamespecialvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element lastEmployee;
      NodeList lastempList;
      Node child;
      String childName;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedResult = new java.util.ArrayList();
      expectedResult.add("employeeId");
      expectedResult.add("name");
      expectedResult.add("position");
      expectedResult.add("salary");
      expectedResult.add("gender");
      expectedResult.add("address");
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      lastEmployee = (Element) elementList.item(4);
      lastempList = lastEmployee.getElementsByTagName("*");
      for (int indexN1006A = 0; indexN1006A < lastempList.getLength(); indexN1006A++) {
          child = (Node) lastempList.item(indexN1006A);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEquals("tagNames", expectedResult, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementsbytagnamespecialvalue.class, args);
   }
}
