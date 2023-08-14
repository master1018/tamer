public final class processinginstructionsetdatanomodificationallowederrEE extends DOMTestCase {
   public processinginstructionsetdatanomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      Node entRef;
      ProcessingInstruction piNode;
      Node appendedChild;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      entRef = doc.createEntityReference("ent4");
      appendedChild = gender.appendChild(entRef);
      entRef = gender.getLastChild();
      assertNotNull("entRefNotNull", entRef);
      piNode = (ProcessingInstruction) entRef.getLastChild();
      assertNotNull("piNodeNotNull", piNode);
      {
         boolean success = false;
         try {
            piNode.setData("newData");
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
        DOMTestCase.doMain(processinginstructionsetdatanomodificationallowederrEE.class, args);
   }
}
