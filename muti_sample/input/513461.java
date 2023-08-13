public final class prefix08 extends DOMTestCase {
   public prefix08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
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
      assertNotNull("entElement", entElement);
      createdNode = doc.createElement("text3");
      {
         boolean success = false;
         try {
            entElement.setPrefix("newPrefix");
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
        DOMTestCase.doMain(prefix08.class, args);
   }
}
