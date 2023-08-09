public final class importNode08 extends DOMTestCase {
   public importNode08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      DocumentFragment docFrag;
      Node aNode;
      boolean hasChild;
      Document ownerDocument;
      DocumentType docType;
      String system;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      docFrag = aNewDoc.createDocumentFragment();
      aNode = doc.importNode(docFrag, false);
      hasChild = aNode.hasChildNodes();
      assertFalse("hasChild", hasChild);
ownerDocument = aNode.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("system", null, null, null, "staffNS.dtd", null, null, null, null, system);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode08.class, args);
   }
}
