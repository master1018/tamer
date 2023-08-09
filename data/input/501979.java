public final class nodecdatasectionnodevalue extends DOMTestCase {
   public nodecdatasectionnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element cdataName;
      NodeList childList;
      Node child;
      String cdataNodeValue;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      cdataName = (Element) elementList.item(1);
      childList = cdataName.getChildNodes();
      child = childList.item(1);
      if ((child == null)) {
          child = doc.createCDATASection("This is a CDATASection with EntityReference number 2 &ent2;");
      }
    cdataNodeValue = child.getNodeValue();
      assertEquals("value", "This is a CDATASection with EntityReference number 2 &ent2;", cdataNodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecdatasectionnodevalue.class, args);
   }
}
