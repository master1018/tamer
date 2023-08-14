public final class documentimportnode02 extends DOMTestCase {
   public documentimportnode02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element element;
      Attr attr;
      Node importedAttr;
      String nodeName;
      int nodeType;
      String nodeValue;
      NodeList addresses;
      Node attrsParent;
      doc = (Document) load("staffNS", true);
      docImported = (Document) load("staff", true);
      addresses = doc.getElementsByTagNameNS("http:
      element = (Element) addresses.item(1);
      attr = element.getAttributeNodeNS("http:
      importedAttr = docImported.importNode(attr, false);
      nodeName = importedAttr.getNodeName();
      nodeType = (int) importedAttr.getNodeType();
      nodeValue = importedAttr.getNodeValue();
      attrsParent = importedAttr.getParentNode();
      assertNull("documentimportnode02_parentNull", attrsParent);
      assertEquals("documentimportnode02_nodeName", "emp:zone", nodeName);
      assertEquals("documentimportnode02_nodeType", 2, nodeType);
      assertEquals("documentimportnode02_nodeValue", "CANADA", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode02.class, args);
   }
}
