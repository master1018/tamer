public final class documentinvalidcharacterexceptioncreatepi1 extends DOMTestCase {
   public documentinvalidcharacterexceptioncreatepi1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
      if (factory.hasFeature("XML", null) != true) {
         throw org.w3c.domts.DOMTestIncompatibleException.incompatibleFeature("XML", null);
      }
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      ProcessingInstruction badPI;
      doc = (Document) load("hc_staff", true);
      if (("text/html".equals(getContentType()))) {
      {
         boolean success = false;
         try {
            badPI = doc.createProcessingInstruction("foo", "data");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
         }
         assertTrue("throw_NOT_SUPPORTED_ERR", success);
      }
} else {
      {
         boolean success = false;
         try {
            badPI = doc.createProcessingInstruction("", "data");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("throw_INVALID_CHARACTER_ERR", success);
      }
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentinvalidcharacterexceptioncreatepi1.class, args);
   }
}
