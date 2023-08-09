public final class documentimportnode12 extends DOMTestCase {
   public documentimportnode12(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node addressElem;
      NodeList addressElemChildren;
      NodeList importedChildren;
      int addressElemLen;
      int importedLen;
      doc = (Document) load("staffNS", true);
      childList = doc.getElementsByTagNameNS("*", "address");
      addressElem = childList.item(0);
      imported = doc.importNode(addressElem, true);
      addressElemChildren = addressElem.getChildNodes();
      importedChildren = imported.getChildNodes();
      addressElemLen = (int) addressElemChildren.getLength();
      importedLen = (int) importedChildren.getLength();
      assertEquals("documentimportnode12", importedLen, addressElemLen);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode12.class, args);
   }
}
