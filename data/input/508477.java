public final class documentimportnode13 extends DOMTestCase {
   public documentimportnode13(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childList;
      Node imported;
      NodeList importedList;
      Node employeeElem;
      int importedLen;
      doc = (Document) load("staffNS", true);
      childList = doc.getElementsByTagNameNS("*", "employee");
      employeeElem = childList.item(0);
      imported = doc.importNode(employeeElem, false);
      importedList = imported.getChildNodes();
      importedLen = (int) importedList.getLength();
      assertEquals("documentimportnode13", 0, importedLen);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode13.class, args);
   }
}
