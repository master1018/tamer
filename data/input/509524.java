public final class nodesetnodevaluenomodificationallowederr extends DOMTestCase {
   public nodesetnodevaluenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node genderNode;
      EntityReference entRef;
      Element entElement;
      CharacterData entElementText;
      int nodeType;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      genderNode = genderList.item(2);
      entRef = (EntityReference) genderNode.getFirstChild();
      assertNotNull("entRefNotNull", entRef);
      nodeType = (int) entRef.getNodeType();
      if (equals(1, nodeType)) {
          entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      }
    entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      entElementText = (CharacterData) entElement.getFirstChild();
      assertNotNull("entElementTextNotNull", entElementText);
      {
         boolean success = false;
         try {
            entElementText.setNodeValue("newValue");
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
        DOMTestCase.doMain(nodesetnodevaluenomodificationallowederr.class, args);
   }
}
