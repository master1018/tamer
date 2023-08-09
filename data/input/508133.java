public final class importNode14 extends DOMTestCase {
   public importNode14(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      ProcessingInstruction pi;
      ProcessingInstruction aNode;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String target;
      String data;
      java.util.List result = new java.util.ArrayList();
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      pi = aNewDoc.createProcessingInstruction("target1", "data1");
      aNode = (ProcessingInstruction) doc.importNode(pi, false);
      ownerDocument = aNode.getOwnerDocument();
      assertNotNull("ownerDocumentNotNull", ownerDocument);
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
target = aNode.getTarget();
      assertEquals("piTarget", "target1", target);
      data = aNode.getData();
      assertEquals("piData", "data1", data);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode14.class, args);
   }
}
