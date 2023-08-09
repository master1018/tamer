public final class elementhasattribute03 extends DOMTestCase {
   public elementhasattribute03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      state = element.hasAttribute("domestic");
      assertFalse("elementhasattribute03_False", state);
newAttribute = element.setAttributeNode(attribute);
      state = element.hasAttribute("domestic");
      assertTrue("elementhasattribute03_True", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattribute03.class, args);
   }
}
