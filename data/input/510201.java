public final class elementinvalidcharacterexception extends DOMTestCase {
   public elementinvalidcharacterexception(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = (Element) elementList.item(0);
      {
         boolean success = false;
         try {
            testAddress.setAttribute("invalid^Name", "value");
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
        DOMTestCase.doMain(elementinvalidcharacterexception.class, args);
   }
}
