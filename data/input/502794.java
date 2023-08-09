public final class nodeentityreferencenodevalue extends DOMTestCase {
   public nodeentityreferencenodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element entRefAddr;
      Node entRefNode;
      String entRefValue;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      entRefAddr = (Element) elementList.item(1);
      entRefNode = entRefAddr.getFirstChild();
      nodeType = (int) entRefNode.getNodeType();
      if (equals(3, nodeType)) {
          entRefNode = doc.createEntityReference("ent2");
      assertNotNull("createdEntRefNotNull", entRefNode);
      }
    entRefValue = entRefNode.getNodeValue();
      assertNull("entRefNodeValue", entRefValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentityreferencenodevalue.class, args);
   }
}
