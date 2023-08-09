public final class hc_elementinvalidcharacterexception1 extends DOMTestCase {
   public hc_elementinvalidcharacterexception1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = (Element) elementList.item(0);
      {
         boolean success = false;
         try {
            testAddress.setAttribute("", "value");
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
        DOMTestCase.doMain(hc_elementinvalidcharacterexception1.class, args);
   }
}
