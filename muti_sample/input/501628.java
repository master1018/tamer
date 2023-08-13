public final class importNode06 extends DOMTestCase {
   public importNode06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      Element element;
      Node aNode;
      boolean hasChild;
      String name;
      Node child;
      String value;
      NodeList addresses;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      addresses = aNewDoc.getElementsByTagName("emp:address");
      element = (Element) addresses.item(0);
      assertNotNull("empAddressNotNull", element);
      aNode = doc.importNode(element, true);
      hasChild = aNode.hasChildNodes();
      assertTrue("throw_True", hasChild);
      name = aNode.getNodeName();
      assertEquals("nodeName", "emp:address", name);
      child = aNode.getFirstChild();
      value = child.getNodeValue();
      assertEquals("nodeValue", "27 South Road. Dallas, texas 98556", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode06.class, args);
   }
}
