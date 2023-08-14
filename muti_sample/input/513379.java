public final class documentimportnode15 extends DOMTestCase {
   public documentimportnode15(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docImp;
      Node textImport;
      Node textToImport;
      String nodeValue;
      doc = (Document) load("staffNS", true);
      docImp = (Document) load("staffNS", true);
      textToImport = doc.createTextNode("Document.importNode test for a TEXT_NODE");
      textImport = doc.importNode(textToImport, true);
      nodeValue = textImport.getNodeValue();
      assertEquals("documentimportnode15", "Document.importNode test for a TEXT_NODE", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode15.class, args);
   }
}
