public final class documentimportnode10 extends DOMTestCase {
   public documentimportnode10(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      DocumentFragment docFragment;
      NodeList childList;
      boolean success;
      Node addressNode;
      Node appendedChild;
      Node importedDocFrag;
      doc = (Document) load("staffNS", true);
      docFragment = doc.createDocumentFragment();
      childList = doc.getElementsByTagNameNS("*", "address");
      addressNode = childList.item(0);
      appendedChild = docFragment.appendChild(addressNode);
      importedDocFrag = doc.importNode(docFragment, true);
      success = importedDocFrag.hasChildNodes();
      assertTrue("documentimportnode10", success);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode10.class, args);
   }
}
