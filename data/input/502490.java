public final class importNode07 extends DOMTestCase {
   public importNode07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      Element element;
      Node aNode;
      NamedNodeMap attributes;
      String name;
      Node attr;
      String lname;
      String namespaceURI = "http:
      String qualifiedName = "emp:employee";
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staff", true);
      element = aNewDoc.createElementNS(namespaceURI, qualifiedName);
      aNode = doc.importNode(element, false);
      attributes = aNode.getAttributes();
      assertSize("throw_Size", 1, attributes);
      name = aNode.getNodeName();
      assertEquals("nodeName", "emp:employee", name);
      attr = attributes.item(0);
      lname = attr.getLocalName();
      assertEquals("lname", "defaultAttr", lname);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode07.class, args);
   }
}
