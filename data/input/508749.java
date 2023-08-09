public final class documentcreateattributeNS05 extends DOMTestCase {
   public documentcreateattributeNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String namespaceURI = null;
      String qualifiedName = "abc:def";
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
         assertTrue("documentcreateattributeNS05", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS05.class, args);
   }
}
