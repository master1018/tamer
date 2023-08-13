public final class domimplementationcreatedocumenttype04 extends DOMTestCase {
   public domimplementationcreatedocumenttype04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      DocumentType newDocType;
      String publicId = "http:
      String systemId = "dom2.dtd";
      String qualifiedName;
      java.util.List qualifiedNames = new java.util.ArrayList();
      qualifiedNames.add("{");
      qualifiedNames.add("}");
      qualifiedNames.add("'");
      qualifiedNames.add("~");
      qualifiedNames.add("`");
      qualifiedNames.add("@");
      qualifiedNames.add("#");
      qualifiedNames.add("$");
      qualifiedNames.add("%");
      qualifiedNames.add("^");
      qualifiedNames.add("&");
      qualifiedNames.add("*");
      qualifiedNames.add("(");
      qualifiedNames.add(")");
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      for (int indexN10073 = 0; indexN10073 < qualifiedNames.size(); indexN10073++) {
          qualifiedName = (String) qualifiedNames.get(indexN10073);
      {
         boolean success = false;
         try {
            newDocType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("domimplementationcreatedocumenttype04", success);
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocumenttype04.class, args);
   }
}
