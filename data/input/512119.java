public final class createAttributeNS06 extends DOMTestCase {
   public createAttributeNS06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName;
      Document doc;
      Attr newAttr;
      doc = (Document) load("hc_staff", true);
      {
         boolean success = false;
         try {
            newAttr = doc.createAttributeNS(namespaceURI, "");
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
        DOMTestCase.doMain(createAttributeNS06.class, args);
   }
}
