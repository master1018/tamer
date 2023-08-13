public final class hc_nodereplacechildnodeancestor extends DOMTestCase {
   public hc_nodereplacechildnodeancestor(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node oldChild;
      Node replacedNode;
      doc = (Document) load("hc_staff", true);
      newChild = doc.getDocumentElement();
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      oldChild = childList.item(0);
      {
         boolean success = false;
         try {
            replacedNode = employeeNode.replaceChild(newChild, oldChild);
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
        DOMTestCase.doMain(hc_nodereplacechildnodeancestor.class, args);
   }
}
