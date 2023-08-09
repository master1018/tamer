public final class nodeentityreferencenodetype extends DOMTestCase {
   public nodeentityreferencenodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element entRefAddr;
      Node entRefNode;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      entRefAddr = (Element) elementList.item(1);
      entRefNode = entRefAddr.getFirstChild();
      nodeType = (int) entRefNode.getNodeType();
      if (equals(3, nodeType)) {
          entRefNode = doc.createEntityReference("ent2");
      assertNotNull("createdEntRefNotNull", entRefNode);
      nodeType = (int) entRefNode.getNodeType();
      }
    assertEquals("entityNodeType", 5, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentityreferencenodetype.class, args);
   }
}
