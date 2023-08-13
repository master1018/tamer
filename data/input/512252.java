public final class nodeappendchildnodeancestor extends DOMTestCase {
   public nodeappendchildnodeancestor(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newChild;
      NodeList elementList;
      Node employeeNode;
      Node appendedChild;
      doc = (Document) load("staff", true);
      newChild = doc.getDocumentElement();
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      {
         boolean success = false;
         try {
            appendedChild = employeeNode.appendChild(newChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchildnodeancestor.class, args);
   }
}
