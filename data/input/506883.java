public final class elementgetelementsbytagnameaccessnodelist extends DOMTestCase {
   public elementgetelementsbytagnameaccessnodelist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Node child;
      String childName;
      String childValue;
      int childType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      testEmployee = (Element) elementList.item(3);
      child = testEmployee.getFirstChild();
      childType = (int) child.getNodeType();
      if (equals(3, childType)) {
          child = child.getNextSibling();
      }
    childName = child.getNodeName();
      assertEquals("nodename", "employeeId", childName);
      child = child.getFirstChild();
      childValue = child.getNodeValue();
      assertEquals("emp0004", "EMP0004", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementsbytagnameaccessnodelist.class, args);
   }
}
