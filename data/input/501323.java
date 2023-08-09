public final class elementsetattributens04 extends DOMTestCase {
   public elementsetattributens04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
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
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      for (int indexN10058 = 0; indexN10058 < qualifiedNames.size(); indexN10058++) {
          qualifiedName = (String) qualifiedNames.get(indexN10058);
      {
         boolean success = false;
         try {
            element.setAttributeNS("http:
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("elementsetattributens04", success);
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens04.class, args);
   }
}
