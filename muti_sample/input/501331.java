public final class getElementsByTagNameNS02 extends DOMTestCase {
   public getElementsByTagNameNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList newList;
      Element newElement;
      String prefix;
      String lname;
      doc = (Document) load("staffNS", false);
      newList = doc.getElementsByTagNameNS("*", "employee");
      assertSize("employeeCount", 5, newList);
      newElement = (Element) newList.item(3);
      prefix = newElement.getPrefix();
      assertEquals("prefix", "emp", prefix);
      lname = newElement.getLocalName();
      assertEquals("lname", "employee", lname);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS02.class, args);
   }
}
