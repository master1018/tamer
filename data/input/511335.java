public final class documentcreateattributeNS04 extends DOMTestCase {
   public documentcreateattributeNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      qualifiedNames.add("_:");
      qualifiedNames.add(":0a");
      qualifiedNames.add(":");
      qualifiedNames.add("a:b:c");
      qualifiedNames.add("_::a");
      doc = (Document) load("staffNS", false);
      for (int indexN1004E = 0; indexN1004E < qualifiedNames.size(); indexN1004E++) {
          qualifiedName = (String) qualifiedNames.get(indexN1004E);
      {
         try {
            attribute = doc.createAttributeNS(namespaceURI, qualifiedName);
             fail("documentcreateattributeNS04");
          } catch (DOMException expected) {
         }
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS04.class, args);
   }
}
