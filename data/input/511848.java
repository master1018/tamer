public final class nodevalue07 extends DOMTestCase {
   public nodevalue07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newNode;
      String newValue;
      NamedNodeMap nodeMap;
      DocumentType docType;
      doc = (Document) load("staff", true);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      nodeMap = docType.getEntities();
      assertNotNull("entitiesNotNull", nodeMap);
      newNode = nodeMap.getNamedItem("ent1");
      assertNotNull("entityNotNull", newNode);
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
        DOMTestCase.doMain(nodevalue07.class, args);
   }
}
