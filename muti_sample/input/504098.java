public final class nodeelementnodeattributes extends DOMTestCase {
   public nodeelementnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      NamedNodeMap addrAttr;
      Node attrNode;
      String attrName;
      java.util.Collection attrList = new java.util.ArrayList();
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("domestic");
      expected.add("street");
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(2);
      addrAttr = testAddr.getAttributes();
      for (int indexN1005C = 0; indexN1005C < addrAttr.getLength(); indexN1005C++) {
          attrNode = (Node) addrAttr.item(indexN1005C);
    attrName = attrNode.getNodeName();
      attrList.add(attrName);
        }
      assertEquals("nodeElementNodeValueAssert1", expected, attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeelementnodeattributes.class, args);
   }
}
