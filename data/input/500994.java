public final class nodegetownerdocument01 extends DOMTestCase {
   public nodegetownerdocument01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document ownerDoc;
      DOMImplementation domImpl;
      DocumentType docType;
      String nullID = null;
      doc = (Document) load("staff", false);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("mydoc", nullID, nullID);
      ownerDoc = docType.getOwnerDocument();
      assertNull("nodegetownerdocument01", ownerDoc);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetownerdocument01.class, args);
   }
}
