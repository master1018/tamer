public final class namednodemapreturnlastitem extends DOMTestCase {
   public namednodemapreturnlastitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Node child;
      String name;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      child = attributes.item(1);
      name = child.getNodeName();
      assertTrue("namednodemapReturnLastItemAssert", 
    (equals("domestic", name) | equals("street", name))
);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapreturnlastitem.class, args);
   }
}
