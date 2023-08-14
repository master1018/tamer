public final class nodeappendchildgetnodename extends DOMTestCase {
   public nodeappendchildgetnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node newChild;
      Node appendNode;
      String childName;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newChild = doc.createElement("newChild");
      appendNode = employeeNode.appendChild(newChild);
      childName = appendNode.getNodeName();
      assertEquals("nodeAppendChildGetNodeNameAssert1", "newChild", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchildgetnodename.class, args);
   }
}
