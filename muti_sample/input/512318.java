public final class processinginstructionsetdatanomodificationallowederr extends DOMTestCase {
   public processinginstructionsetdatanomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notExpandEntityReferences
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      Node entRef;
      ProcessingInstruction piNode;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      entRef = gender.getFirstChild();
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
        DOMTestCase.doMain(processinginstructionsetdatanomodificationallowederr.class, args);
   }
}
