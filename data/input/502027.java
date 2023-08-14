public final class documentimportnode01 extends DOMTestCase {
   public documentimportnode01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
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
      attr = element.getAttributeNode("street");
      importedAttr = doc.importNode(attr, false);
      nodeName = importedAttr.getNodeName();
      nodeValue = importedAttr.getNodeValue();
      nodeType = (int) importedAttr.getNodeType();
      assertEquals("documentimportnode01_nodeName", "street", nodeName);
      assertEquals("documentimportnode01_nodeType", 2, nodeType);
      assertEquals("documentimportnode01_nodeValue", "Yes", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode01.class, args);
   }
}
