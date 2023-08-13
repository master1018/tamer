public final class characterdatareplacedatanomodificationallowederrEE extends DOMTestCase {
   public characterdatareplacedatanomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node genderNode;
      CharacterData entText;
      EntityReference entReference;
      Node appendedNode;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      genderNode = genderList.item(2);
      entReference = doc.createEntityReference("ent3");
      assertNotNull("createdEntRefNotNull", entReference);
      appendedNode = genderNode.appendChild(entReference);
      entText = (CharacterData) entReference.getFirstChild();
      assertNotNull("entTextNotNull", entText);
      {
         boolean success = false;
         try {
            entText.replaceData(1, 3, "newArg");
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
        DOMTestCase.doMain(characterdatareplacedatanomodificationallowederrEE.class, args);
   }
}
