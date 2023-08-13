public final class setAttributeNS01 extends DOMTestCase {
   public setAttributeNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "emp:qual?name";
      Document doc;
      NodeList elementList;
      Node testAddr;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("employee");
      testAddr = elementList.item(0);
      {
         boolean success = false;
         try {
            ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("throw_INVALID_CHARACTER_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNS01.class, args);
   }
}
