public final class nodeprocessinginstructionnodeattributes extends DOMTestCase {
   public nodeprocessinginstructionnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node piNode;
      NamedNodeMap attrList;
      doc = (Document) load("staff", false);
      testList = doc.getChildNodes();
      piNode = testList.item(0);
      attrList = piNode.getAttributes();
      assertNull("nodeProcessingInstructionNodeAttrAssert1", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeprocessinginstructionnodeattributes.class, args);
   }
}
