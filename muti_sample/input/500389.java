public final class elementsetattributens08 extends DOMTestCase {
   public elementsetattributens08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      {
         boolean success = false;
         try {
            element.setAttributeNS("http:
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("elementsetattributens08_Err1", success);
      }
      {
         boolean success = false;
         try {
            element.setAttributeNS("http:
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("elementsetattributens08_Err2", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens08.class, args);
   }
}
