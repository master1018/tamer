public final class importNode05 extends DOMTestCase {
   public importNode05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      Element element;
      Node aNode;
      boolean hasChild;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String name;
      NodeList addresses;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      addresses = aNewDoc.getElementsByTagName("emp:address");
      element = (Element) addresses.item(0);
      assertNotNull("empAddressNotNull", element);
      aNode = doc.importNode(element, false);
      hasChild = aNode.hasChildNodes();
      assertFalse("hasChild", hasChild);
ownerDocument = aNode.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("dtdSystemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
name = aNode.getNodeName();
      assertEquals("nodeName", "emp:address", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode05.class, args);
   }
}
