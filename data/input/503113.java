public final class prefix11 extends DOMTestCase {
   public prefix11(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      String namespaceURI;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      namespaceURI = employeeNode.getNamespaceURI();
      {
         boolean success = false;
         try {
            employeeNode.setPrefix("employee1");
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
        DOMTestCase.doMain(prefix11.class, args);
   }
}
