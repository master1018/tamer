public final class elementhasattribute04 extends DOMTestCase {
   public elementhasattribute04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      boolean state;
      Attr attribute;
      Attr newAttribute;
      doc = (Document) load("staff", false);
      element = doc.createElement("address");
      attribute = doc.createAttribute("domestic");
      newAttribute = element.setAttributeNode(attribute);
      state = element.hasAttribute("domestic");
      assertTrue("elementhasattribute04", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattribute04.class, args);
   }
}
