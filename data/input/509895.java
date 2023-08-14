public final class documentcreateelementNS01 extends DOMTestCase {
   public documentcreateelementNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String namespaceURI = "http:
      String qualifiedName = "XML:XML";
      String nodeName;
      String nsURI;
      String localName;
      String prefix;
      String tagName;
      doc = (Document) load("staffNS", false);
      element = doc.createElementNS(namespaceURI, qualifiedName);
      nodeName = element.getNodeName();
      nsURI = element.getNamespaceURI();
      localName = element.getLocalName();
      prefix = element.getPrefix();
      tagName = element.getTagName();
      assertEquals("documentcreateelementNS01_nodeName", "XML:XML", nodeName);
      assertEquals("documentcreateelementNS01_namespaceURI", "http:
      assertEquals("documentcreateelementNS01_localName", "XML", localName);
      assertEquals("documentcreateelementNS01_prefix", "XML", prefix);
      assertEquals("documentcreateelementNS01_tagName", "XML:XML", tagName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementNS01.class, args);
   }
}
