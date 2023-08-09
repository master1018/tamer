public final class elementsetattributensurinull extends DOMTestCase {
   public elementsetattributensurinull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = null;
      String qualifiedName = "emp:qualifiedName";
      Document doc;
      NodeList elementList;
      Node testAddr;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      testAddr = elementList.item(0);
      {
         boolean success = false;
         try {
            ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
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
        DOMTestCase.doMain(elementsetattributensurinull.class, args);
   }
}
