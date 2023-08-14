public final class localName02 extends DOMTestCase {
   public localName02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node createdNode;
      String localName;
      doc = (Document) load("staffNS", false);
      createdNode = doc.createElement("test:employee");
      localName = createdNode.getLocalName();
      assertNull("localNameNull", localName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(localName02.class, args);
   }
}
