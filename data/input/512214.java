public final class nodeappendchildchildexists extends DOMTestCase {
   public nodeappendchildchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node childNode;
      Node newChild;
      Node lchild;
      Node fchild;
      String lchildName;
      String fchildName;
      Node appendedChild;
      String initialName;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      childNode = elementList.item(1);
      newChild = childNode.getFirstChild();
      initialName = newChild.getNodeName();
      appendedChild = childNode.appendChild(newChild);
      fchild = childNode.getFirstChild();
      fchildName = fchild.getNodeName();
      lchild = childNode.getLastChild();
      lchildName = lchild.getNodeName();
      if (equals("employeeId", initialName)) {
          assertEquals("assert1_nowhitespace", "name", fchildName);
      assertEquals("assert2_nowhitespace", "employeeId", lchildName);
      } else {
          assertEquals("assert1", "employeeId", fchildName);
      assertEquals("assert2", "#text", lchildName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchildchildexists.class, args);
   }
}
