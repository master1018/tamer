public final class documentcreateattributeNS06 extends DOMTestCase {
   public documentcreateattributeNS06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document newDoc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Attr attribute;
      String namespaceURI = "http:
      String qualifiedName = "xml:root";
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      newDoc = domImpl.createDocument("http:
      {
         boolean success = false;
         try {
            attribute = newDoc.createAttributeNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("documentcreateattributeNS06", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS06.class, args);
   }
}
