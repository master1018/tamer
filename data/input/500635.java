public final class hc_elementgetelementsbytagnameaccessnodelist extends DOMTestCase {
   public hc_elementgetelementsbytagnameaccessnodelist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Node firstC;
      String childName;
      int nodeType;
      CharacterData employeeIDNode;
      String employeeID;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      testEmployee = (Element) elementList.item(3);
      firstC = testEmployee.getFirstChild();
      nodeType = (int) firstC.getNodeType();
    while (equals(3, nodeType)) {
    firstC = firstC.getNextSibling();
      nodeType = (int) firstC.getNodeType();
    }
childName = firstC.getNodeName();
      assertEqualsAutoCase("element", "childName", "em", childName);
        employeeIDNode = (CharacterData) firstC.getFirstChild();
      employeeID = employeeIDNode.getNodeValue();
      assertEquals("employeeID", "EMP0004", employeeID);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetelementsbytagnameaccessnodelist.class, args);
   }
}
