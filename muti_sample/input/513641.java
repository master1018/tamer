public final class documentcreateattributeNS03 extends DOMTestCase {
   public documentcreateattributeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute;
      String namespaceURI = "http:
      String qualifiedName;
      java.util.List qualifiedNames = new java.util.ArrayList();
      qualifiedNames.add("/");
      qualifiedNames.add("
      qualifiedNames.add("\\");
      qualifiedNames.add(";");
      qualifiedNames.add("&");
      qualifiedNames.add("*");
      qualifiedNames.add("]]");
      qualifiedNames.add(">");
      qualifiedNames.add("<");
      doc = (Document) load("staffNS", false);
      for (int indexN1005A = 0; indexN1005A < qualifiedNames.size(); indexN1005A++) {
          qualifiedName = (String) qualifiedNames.get(indexN1005A);
      {
         boolean success = false;
         try {
            attribute = doc.createAttributeNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("documentcreateattributeNS03", success);
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS03.class, args);
   }
}
