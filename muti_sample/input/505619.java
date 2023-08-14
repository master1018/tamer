public final class namednodemapreturnfirstitem extends DOMTestCase {
   public namednodemapreturnfirstitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node child;
      String name;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(1);
      attributes = testAddress.getAttributes();
      child = attributes.item(0);
      name = child.getNodeName();
      assertTrue("namednodemapReturnFirstItemAssert", 
    (equals("domestic", name) | equals("street", name))
);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapreturnfirstitem.class, args);
   }
}
