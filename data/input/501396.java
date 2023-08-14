public final class documentcreateattributeNS01 extends DOMTestCase {
   public documentcreateattributeNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute;
      String namespaceURI = null;
      String qualifiedName = "test";
      String name;
      String nodeName;
      String nodeValue;
      doc = (Document) load("staffNS", false);
      attribute = doc.createAttributeNS(namespaceURI, qualifiedName);
      nodeName = attribute.getNodeName();
      nodeValue = attribute.getNodeValue();
      assertEquals("documentcreateattributeNS01", "test", nodeName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS01.class, args);
   }
}
