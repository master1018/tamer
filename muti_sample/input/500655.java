public final class setNamedItemNS03 extends DOMTestCase {
   public setNamedItemNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "prefix:newAttr";
      Document doc;
      Node arg;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node retnode;
      String value;
      Node setNode;
      doc = (Document) load("staffNS", true);
      arg = doc.createAttributeNS(namespaceURI, qualifiedName);
      arg.setNodeValue("newValue");
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(0);
      attributes = testAddress.getAttributes();
      setNode = attributes.setNamedItemNS(arg);
      retnode = attributes.getNamedItemNS(namespaceURI, "newAttr");
      value = retnode.getNodeValue();
      assertEquals("throw_Equals", "newValue", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setNamedItemNS03.class, args);
   }
}
