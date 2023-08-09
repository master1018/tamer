public final class nodedocumentnodeattribute extends DOMTestCase {
   public nodedocumentnodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap attrList;
      doc = (Document) load("staff", false);
      attrList = doc.getAttributes();
      assertNull("documentAttributesNull", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentnodeattribute.class, args);
   }
}
