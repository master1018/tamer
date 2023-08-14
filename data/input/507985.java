public final class createDocumentType03 extends DOMTestCase {
   public createDocumentType03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "prefix:myDoc";
      String publicId = "http:
      String systemId = "myDoc.dtd";
      Document doc;
      DOMImplementation domImpl;
      DocumentType newType = null;
      String nodeName;
      String nodeValue;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      newType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
      nodeName = newType.getNodeName();
      assertEquals("nodeName", "prefix:myDoc", nodeName);
      nodeValue = newType.getNodeValue();
      assertNull("nodeValue", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createDocumentType03.class, args);
   }
}
