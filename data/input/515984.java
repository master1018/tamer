public final class nodegetownerdocument02 extends DOMTestCase {
   public nodegetownerdocument02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Document newDoc;
      Element newElem;
      Document ownerDocDoc;
      Document ownerDocElem;
      DOMImplementation domImpl;
      DocumentType docType;
      String nullNS = null;
      doc = (Document) load("staff", false);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("mydoc", nullNS, nullNS);
      newDoc = domImpl.createDocument("http:
      ownerDocDoc = newDoc.getOwnerDocument();
      assertNull("nodegetownerdocument02_1", ownerDocDoc);
      newElem = newDoc.createElementNS("http:
      ownerDocElem = newElem.getOwnerDocument();
      assertNotNull("nodegetownerdocument02_2", ownerDocElem);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetownerdocument02.class, args);
   }
}
