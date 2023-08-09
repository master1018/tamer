public final class noderemovechildnode extends DOMTestCase {
   public noderemovechildnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node child;
      String childName;
      int length;
      Node removedChild;
      String removedName;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      oldChild = childList.item(0);
      removedChild = employeeNode.removeChild(oldChild);
      removedName = removedChild.getNodeName();
      child = childList.item(0);
      childName = child.getNodeName();
      length = (int) childList.getLength();
      if (equals(5, length)) {
          assertEquals("removedNameNoWhitespace", "employeeId", removedName);
      assertEquals("childNameNoWhitespace", "name", childName);
      } else {
          assertEquals("removedName", "#text", removedName);
      assertEquals("childName", "employeeId", childName);
      assertEquals("length", 12, length);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(noderemovechildnode.class, args);
   }
}
