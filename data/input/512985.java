public final class hc_nodeinsertbeforenodeancestor extends DOMTestCase {
   public hc_nodeinsertbeforenodeancestor(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newChild;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node refChild;
      Node insertedNode;
      doc = (Document) load("hc_staff", true);
      newChild = doc.getDocumentElement();
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      refChild = childList.item(0);
      {
         boolean success = false;
         try {
            insertedNode = employeeNode.insertBefore(newChild, refChild);
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
        DOMTestCase.doMain(hc_nodeinsertbeforenodeancestor.class, args);
   }
}
