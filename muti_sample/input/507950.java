public final class hc_nodeinsertbeforeinvalidnodetype extends DOMTestCase {
   public hc_nodeinsertbeforeinvalidnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      Node newChild;
      NodeList elementList;
      Node refChild;
      Node insertedNode;
      doc = (Document) load("hc_staff", true);
      newChild = doc.createAttribute("title");
      elementList = doc.getElementsByTagName("p");
      refChild = elementList.item(1);
      rootNode = (Element) refChild.getParentNode();
      {
         boolean success = false;
         try {
            insertedNode = rootNode.insertBefore(newChild, refChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbeforeinvalidnodetype.class, args);
   }
}
