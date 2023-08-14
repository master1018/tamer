public final class nodecommentnodeattributes extends DOMTestCase {
   public nodecommentnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childList;
      Node childNode;
      NamedNodeMap attrList;
      int nodeType;
      doc = (Document) load("staff", false);
      childList = doc.getChildNodes();
      for (int indexN10043 = 0; indexN10043 < childList.getLength(); indexN10043++) {
          childNode = (Node) childList.item(indexN10043);
    nodeType = (int) childNode.getNodeType();
      if (equals(8, nodeType)) {
          attrList = childNode.getAttributes();
      assertNull("attributesNull", attrList);
      }
      }
      childNode = doc.createComment("This is a comment");
      attrList = childNode.getAttributes();
      assertNull("createdAttributesNull", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecommentnodeattributes.class, args);
   }
}
