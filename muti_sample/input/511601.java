public final class importNode03 extends DOMTestCase {
   public importNode03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      Comment comment;
      Node aNode;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String value;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      comment = aNewDoc.createComment("this is a comment");
      aNode = doc.importNode(comment, false);
      ownerDocument = aNode.getOwnerDocument();
      assertNotNull("ownerDocumentNotNull", ownerDocument);
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
value = aNode.getNodeValue();
      assertEquals("nodeValue", "this is a comment", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode03.class, args);
   }
}
