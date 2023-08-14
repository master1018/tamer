public final class getElementsByTagNameNS09 extends DOMTestCase {
   public getElementsByTagNameNS09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element docElem;
      doc = (Document) load("staffNS", false);
      docElem = doc.getDocumentElement();
      newList = docElem.getElementsByTagNameNS("*", "employee");
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
        DOMTestCase.doMain(getElementsByTagNameNS09.class, args);
   }
}
