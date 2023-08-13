public final class setNamedItemNS05 extends DOMTestCase {
   public setNamedItemNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "dmstc:domestic";
      Document doc;
      Node arg;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node retnode;
      String value;
      doc = (Document) load("staffNS", true);
      arg = doc.createAttributeNS(namespaceURI, qualifiedName);
      arg.setNodeValue("newValue");
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(0);
      attributes = testAddress.getAttributes();
      retnode = attributes.setNamedItemNS(arg);
      value = retnode.getNodeValue();
      assertEquals("throw_Equals", "Yes", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setNamedItemNS05.class, args);
   }
}
