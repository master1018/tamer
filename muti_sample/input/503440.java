public final class nodereplacechildnomodificationallowederr extends DOMTestCase {
   public nodereplacechildnomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node genderNode;
      Node entRef;
      Node entElement;
      Node createdNode;
      Node replacedChild;
      int nodeType;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      genderNode = genderList.item(2);
      entRef = genderNode.getFirstChild();
      nodeType = (int) entRef.getNodeType();
      if (equals(1, nodeType)) {
          entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      }
    entElement = entRef.getFirstChild();
      createdNode = doc.createElement("newChild");
      {
         boolean success = false;
         try {
            replacedChild = entRef.replaceChild(createdNode, entElement);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodereplacechildnomodificationallowederr.class, args);
   }
}
