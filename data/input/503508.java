public final class prefix07 extends DOMTestCase {
   public prefix07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(0);
      {
         boolean success = false;
         try {
            employeeNode.setPrefix("emp::");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("throw_NAMESPACE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(prefix07.class, args);
   }
}
