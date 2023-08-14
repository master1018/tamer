public final class documentimportnode05 extends DOMTestCase {
   public documentimportnode05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docImported;
      Attr attr;
      Node importedAttr;
      String nodeName;
      int nodeType;
      String nodeValue;
      String namespaceURI;
      doc = (Document) load("staffNS", true);
      docImported = (Document) load("staff", true);
      attr = doc.createAttributeNS("http:
      importedAttr = docImported.importNode(attr, false);
      nodeName = importedAttr.getNodeName();
      nodeValue = importedAttr.getNodeValue();
      nodeType = (int) importedAttr.getNodeType();
      namespaceURI = importedAttr.getNamespaceURI();
      assertEquals("documentimportnode05_nodeName", "a_:b0", nodeName);
      assertEquals("documentimportnode05_nodeType", 2, nodeType);
      assertEquals("documentimportnode05_nodeValue", "", nodeValue);
      assertEquals("documentimportnode05_namespaceURI", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode05.class, args);
   }
}
