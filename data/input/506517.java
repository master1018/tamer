public final class prefix01 extends DOMTestCase {
   public prefix01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node createdNode;
      String prefix;
      doc = (Document) load("staffNS", false);
      createdNode = doc.createElement("test:employee");
      prefix = createdNode.getPrefix();
      assertNull("throw_Null", prefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(prefix01.class, args);
   }
}
