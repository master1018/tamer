public final class hc_nodecommentnodeattributes extends DOMTestCase {
   public hc_nodecommentnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node commentNode;
      NodeList nodeList;
      NamedNodeMap attrList;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      nodeList = doc.getChildNodes();
      for (int indexN10043 = 0; indexN10043 < nodeList.getLength(); indexN10043++) {
          commentNode = (Node) nodeList.item(indexN10043);
    nodeType = (int) commentNode.getNodeType();
      if (equals(8, nodeType)) {
          attrList = commentNode.getAttributes();
      assertNull("existingCommentAttributesNull", attrList);
      }
      }
      commentNode = doc.createComment("This is a comment");
      attrList = commentNode.getAttributes();
      assertNull("createdCommentAttributesNull", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodecommentnodeattributes.class, args);
   }
}
