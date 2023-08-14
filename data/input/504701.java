public final class documentimportnode11 extends DOMTestCase {
   public documentimportnode11(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element docElement;
      Node imported;
      boolean success;
      String nodeNameOrig;
      String nodeNameImported;
      doc = (Document) load("staffNS", true);
      docElement = doc.getDocumentElement();
      imported = doc.importNode(docElement, false);
      success = imported.hasChildNodes();
      assertFalse("documentimportnode11", success);
nodeNameImported = imported.getNodeName();
      nodeNameOrig = docElement.getNodeName();
      assertEquals("documentimportnode11_NodeName", nodeNameImported, nodeNameOrig);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode11.class, args);
   }
}
