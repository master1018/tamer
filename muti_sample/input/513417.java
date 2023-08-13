public final class importNode04 extends DOMTestCase {
   public importNode04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      DocumentFragment docFrag;
      Comment comment;
      Node aNode;
      NodeList children;
      Node child;
      String childValue;
      doc = (Document) load("staff", true);
      aNewDoc = (Document) load("staff", true);
      docFrag = aNewDoc.createDocumentFragment();
      comment = aNewDoc.createComment("descendant1");
      aNode = docFrag.appendChild(comment);
      aNode = doc.importNode(docFrag, true);
      children = aNode.getChildNodes();
      assertSize("throw_Size", 1, children);
      child = aNode.getFirstChild();
      childValue = child.getNodeValue();
      assertEquals("descendant1", "descendant1", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode04.class, args);
   }
}
