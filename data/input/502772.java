public final class noderemovechildgetnodename extends DOMTestCase {
   public noderemovechildgetnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild;
      Node removedChild;
      String childName;
      int length;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      oldChild = childList.item(0);
      removedChild = employeeNode.removeChild(oldChild);
      childName = removedChild.getNodeName();
      if (equals(6, length)) {
          assertEquals("nowhitespace", "employeeId", childName);
      } else {
          assertEquals("whitespace", "#text", childName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(noderemovechildgetnodename.class, args);
   }
}
