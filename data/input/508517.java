public final class importNode02 extends DOMTestCase {
   public importNode02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      CDATASection cDataSec;
      Node aNode;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String value;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      cDataSec = aNewDoc.createCDATASection("this is CDATASection data");
      aNode = doc.importNode(cDataSec, false);
      ownerDocument = aNode.getOwnerDocument();
      assertNotNull("ownerDocumentNotNull", ownerDocument);
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("dtdSystemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
value = aNode.getNodeValue();
      assertEquals("nodeValue", "this is CDATASection data", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode02.class, args);
   }
}
