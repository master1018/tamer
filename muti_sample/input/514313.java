public final class documentcreateattributeNS02 extends DOMTestCase {
   public documentcreateattributeNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute1;
      Attr attribute2;
      String name;
      String nodeName;
      String nodeValue;
      String prefix;
      String namespaceURI;
      doc = (Document) load("staffNS", false);
      attribute1 = doc.createAttributeNS("http:
      name = attribute1.getName();
      nodeName = attribute1.getNodeName();
      nodeValue = attribute1.getNodeValue();
      prefix = attribute1.getPrefix();
      namespaceURI = attribute1.getNamespaceURI();
      assertEquals("documentcreateattributeNS02_att1_name", "xml:xml", name);
      assertEquals("documentcreateattributeNS02_att1_nodeName", "xml:xml", nodeName);
      assertEquals("documentcreateattributeNS02_att1_nodeValue", "", nodeValue);
      assertEquals("documentcreateattributeNS02_att1_prefix", "xml", prefix);
      assertEquals("documentcreateattributeNS02_att1_namespaceURI", "http:
      attribute2 = doc.createAttributeNS("http:
      name = attribute2.getName();
      nodeName = attribute2.getNodeName();
      nodeValue = attribute2.getNodeValue();
      prefix = attribute2.getPrefix();
      namespaceURI = attribute2.getNamespaceURI();
      assertEquals("documentcreateattributeNS02_att2_name", "xmlns", name);
      assertEquals("documentcreateattributeNS02_att2_nodeName", "xmlns", nodeName);
      assertEquals("documentcreateattributeNS02_att2_nodeValue", "", nodeValue);
      assertEquals("documentcreateattributeNS02_att2_namespaceURI", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattributeNS02.class, args);
   }
}
