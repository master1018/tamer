public final class characterdatareplacedatanomodificationallowederr extends DOMTestCase {
   public characterdatareplacedatanomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node genderNode;
      Node entElement;
      Node entElementContent;
      Node entReference;
      int nodeType;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      genderNode = genderList.item(2);
      entReference = genderNode.getFirstChild();
      assertNotNull("entReferenceNotNull", entReference);
      nodeType = (int) entReference.getNodeType();
      if (equals(1, nodeType)) {
          entReference = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entReference);
      }
    entElement = entReference.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      entElementContent = entElement.getFirstChild();
      assertNotNull("entElementContentNotNull", entElementContent);
      {
         boolean success = false;
         try {
            ((CharacterData) entElementContent).replaceData(1, 3, "newArg");
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
        DOMTestCase.doMain(characterdatareplacedatanomodificationallowederr.class, args);
   }
}
