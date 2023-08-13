public final class documentimportnode18 extends DOMTestCase {
   public documentimportnode18(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      ProcessingInstruction piImport;
      ProcessingInstruction piToImport;
      String piData;
      String piTarget;
      doc = (Document) load("staffNS", true);
      docImp = (Document) load("staffNS", true);
      piToImport = doc.createProcessingInstruction("Target", "Data");
      piImport = (ProcessingInstruction) doc.importNode(piToImport, false);
      piTarget = piImport.getTarget();
      piData = piImport.getData();
      assertEquals("documentimportnode18_Target", "Target", piTarget);
      assertEquals("documentimportnode18_Data", "Data", piData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode18.class, args);
   }
}
