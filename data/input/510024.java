public final class namednodemapchildnoderange extends DOMTestCase {
   public namednodemapchildnoderange(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int length;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(2);
      attributes = testEmployee.getAttributes();
      length = (int) attributes.getLength();
      assertEquals("length", 2, length);
      child = attributes.item(0);
      child = attributes.item(1);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapchildnoderange.class, args);
   }
}
