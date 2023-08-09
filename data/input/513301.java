public final class setAttributeNS10 extends DOMTestCase {
   public setAttributeNS10(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      Document doc;
      NodeList elementList;
      Node testAddr;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("em");
      testAddr = elementList.item(0);
      {
         boolean success = false;
         try {
            ((Element) testAddr).setAttributeNS(namespaceURI, "", "newValue");
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
        DOMTestCase.doMain(setAttributeNS10.class, args);
   }
}
