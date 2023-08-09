public final class nodetextnodeattribute extends DOMTestCase {
   public nodetextnodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddr;
      Node textNode;
      NamedNodeMap attrList;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = elementList.item(0);
      textNode = testAddr.getFirstChild();
      attrList = textNode.getAttributes();
      assertNull("nodeTextNodeAttributesAssert1", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodetextnodeattribute.class, args);
   }
}
