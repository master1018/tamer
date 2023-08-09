public final class cdatasectiongetdata extends DOMTestCase {
   public cdatasectiongetdata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList nameList;
      Node child;
      Node lastChild;
      String data;
      int nodeType;
      doc = (Document) load("staff", false);
      nameList = doc.getElementsByTagName("name");
      child = nameList.item(1);
      lastChild = child.getLastChild();
      nodeType = (int) lastChild.getNodeType();
      assertEquals("isCDATA", 4, nodeType);
      data = ((CharacterData) lastChild).getData();
      assertEquals("data", "This is an adjacent CDATASection with a reference to a tab &tab;", data);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(cdatasectiongetdata.class, args);
   }
}
