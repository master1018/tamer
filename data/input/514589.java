public final class nodeinsertbeforenomodificationallowederr extends DOMTestCase {
   public nodeinsertbeforenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node insertedNode;
      Node refChild = null;
      int nodeType;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      genderNode = genderList.item(2);
      entRef = genderNode.getFirstChild();
      assertNotNull("entRefNotNull", entRef);
      nodeType = (int) entRef.getNodeType();
      if (equals(1, nodeType)) {
          entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      }
    entElement = entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      createdNode = doc.createElement("text3");
      {
         boolean success = false;
         try {
            insertedNode = entElement.insertBefore(createdNode, refChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NOT_MODIFICATION_ALLOWED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbeforenomodificationallowederr.class, args);
   }
}
