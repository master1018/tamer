public final class ownerElement02 extends DOMTestCase {
   public ownerElement02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttr;
      Element elementNode;
      doc = (Document) load("staff", false);
      newAttr = doc.createAttribute("newAttribute");
      elementNode = newAttr.getOwnerElement();
      assertNull("throw_Null", elementNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(ownerElement02.class, args);
   }
}
