public final class createElementNS06 extends DOMTestCase {
   public createElementNS06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName;
      Document doc;
      boolean done;
      Element newElement;
      String charact;
      doc = (Document) load("hc_staff", true);
      {
         boolean success = false;
         try {
            newElement = doc.createElementNS(namespaceURI, "");
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
        DOMTestCase.doMain(createElementNS06.class, args);
   }
}
