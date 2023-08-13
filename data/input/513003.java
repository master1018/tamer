public final class documentimportnode03 extends DOMTestCase {
   public documentimportnode03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      Attr attr;
      NodeList childList;
      Node importedAttr;
      String nodeName;
      int nodeType;
      String nodeValue;
      doc = (Document) load("staffNS", true);
      childList = doc.getElementsByTagNameNS("http:
      element = (Element) childList.item(1);
      attr = element.getAttributeNode("defaultAttr");
      importedAttr = doc.importNode(attr, false);
      nodeName = importedAttr.getNodeName();
      nodeValue = importedAttr.getNodeValue();
      nodeType = (int) importedAttr.getNodeType();
      assertEquals("documentimportnode03_nodeName", "defaultAttr", nodeName);
      assertEquals("documentimportnode03_nodeType", 2, nodeType);
      assertEquals("documentimportnode03_nodeValue", "defaultVal", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode03.class, args);
   }
}
