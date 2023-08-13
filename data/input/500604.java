public final class nodeentityreferencenodename extends DOMTestCase {
   public nodeentityreferencenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element entRefAddr;
      Node entRefNode;
      String entRefName;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      entRefAddr = (Element) elementList.item(1);
      entRefNode = entRefAddr.getFirstChild();
      nodeType = (int) entRefNode.getNodeType();
      if (!equals(5, nodeType)) {
          entRefNode = doc.createEntityReference("ent2");
      assertNotNull("createdEntRefNotNull", entRefNode);
      }
    entRefName = entRefNode.getNodeName();
      assertEquals("nodeEntityReferenceNodeNameAssert1", "ent2", entRefName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentityreferencenodename.class, args);
   }
}
