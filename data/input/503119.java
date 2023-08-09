public final class nodesetprefix07 extends DOMTestCase {
   public nodesetprefix07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute;
      doc = (Document) load("staffNS", true);
      attribute = doc.createAttributeNS("http:
      {
         boolean success = false;
         try {
            attribute.setPrefix("xmlns");
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
        DOMTestCase.doMain(nodesetprefix07.class, args);
   }
}
