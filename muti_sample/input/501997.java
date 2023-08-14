public final class importNode10 extends DOMTestCase {
   public importNode10(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      EntityReference entRef;
      Node aNode;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String name;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      entRef = aNewDoc.createEntityReference("entRef1");
      assertNotNull("createdEntRefNotNull", entRef);
      entRef.setNodeValue("entRef1Value");
      aNode = doc.importNode(entRef, false);
      ownerDocument = aNode.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
name = aNode.getNodeName();
      assertEquals("nodeName", "entRef1", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode10.class, args);
   }
}
