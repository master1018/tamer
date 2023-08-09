public final class nodecdatasectionnodetype extends DOMTestCase {
   public nodecdatasectionnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notCoalescing
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testName;
      Node cdataNode;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      testName = (Element) elementList.item(1);
      cdataNode = testName.getLastChild();
      nodeType = (int) cdataNode.getNodeType();
      if (equals(3, nodeType)) {
          cdataNode = doc.createCDATASection("");
      nodeType = (int) cdataNode.getNodeType();
      }
    assertEquals("nodeTypeCDATA", 4, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecdatasectionnodetype.class, args);
   }
}
