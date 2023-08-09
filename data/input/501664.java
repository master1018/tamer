public final class documentimportnode17 extends DOMTestCase {
   public documentimportnode17(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node commentImport;
      Node commentToImport;
      String nodeValue;
      doc = (Document) load("staffNS", true);
      docImp = (Document) load("staffNS", true);
      commentToImport = doc.createComment("Document.importNode test for a COMMENT_NODE");
      commentImport = doc.importNode(commentToImport, true);
      nodeValue = commentImport.getNodeValue();
      assertEquals("documentimportnode17", "Document.importNode test for a COMMENT_NODE", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode17.class, args);
   }
}
