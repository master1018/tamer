public final class elementsetattributens05 extends DOMTestCase {
   public elementsetattributens05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      {
         boolean success = false;
         try {
            element.setAttributeNS(nullNS, "dom:root", "test");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("elementsetattributens05", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens05.class, args);
   }
}
