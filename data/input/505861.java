public final class nodevalue08 extends DOMTestCase {
   public nodevalue08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      Node newNode;
      String newValue;
      NamedNodeMap nodeMap;
      doc = (Document) load("staff", true);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      nodeMap = docType.getNotations();
      assertNotNull("notationsNotNull", nodeMap);
      newNode = nodeMap.getNamedItem("notation1");
      assertNotNull("notationNotNull", newNode);
      newValue = newNode.getNodeValue();
      assertNull("initiallyNull", newValue);
      newNode.setNodeValue("This should have no effect");
      newValue = newNode.getNodeValue();
      assertNull("nullAfterAttemptedChange", newValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodevalue08.class, args);
   }
}
