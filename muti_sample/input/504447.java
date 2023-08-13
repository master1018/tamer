public final class documentcreateattributeNS07 extends DOMTestCase {
   public documentcreateattributeNS07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute;
      String namespaceURI = "http:
      String qualifiedName = "xmlns";
      doc = (Document) load("staffNS", false);
      {
         boolean success = false;
         try {
            attribute = doc.createAttributeNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("documentcreateattributeNS07", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS07.class, args);
   }
}
