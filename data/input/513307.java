public final class createDocument07 extends DOMTestCase {
   public createDocument07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "y:x";
      Document doc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Document aNewDoc;
      String nodeName;
      String nodeValue;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      aNewDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
      nodeName = aNewDoc.getNodeName();
      nodeValue = aNewDoc.getNodeValue();
      assertEquals("nodeName", "#document", nodeName);
      assertNull("nodeValue", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createDocument07.class, args);
   }
}
