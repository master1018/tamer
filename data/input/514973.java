public final class importNode13 extends DOMTestCase {
   public importNode13(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      DocumentType doc1Type;
      NamedNodeMap notationList;
      Notation notation;
      Notation aNode;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String publicVal;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      doc1Type = aNewDoc.getDoctype();
      notationList = doc1Type.getNotations();
      assertNotNull("notationsNotNull", notationList);
      notation = (Notation) notationList.getNamedItem("notation1");
      aNode = (Notation) doc.importNode(notation, false);
      ownerDocument = aNode.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
publicVal = aNode.getPublicId();
      assertEquals("publicId", "notation1File", publicVal);
      system = aNode.getSystemId();
      assertNull("notationSystemId", system);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode13.class, args);
   }
}
