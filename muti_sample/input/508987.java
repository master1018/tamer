public final class nodeentityreferencenodeattributes extends DOMTestCase {
   public nodeentityreferencenodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element entRefAddr;
      Node entRefNode;
      NamedNodeMap attrList;
      int nodeType;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      entRefAddr = (Element) elementList.item(1);
      entRefNode = entRefAddr.getFirstChild();
      nodeType = (int) entRefNode.getNodeType();
      if (!equals(5, nodeType)) {
          entRefNode = doc.createEntityReference("ent2");
      assertNotNull("createdEntRefNotNull", entRefNode);
      }
    attrList = entRefNode.getAttributes();
      assertNull("attrList", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentityreferencenodeattributes.class, args);
   }
}
