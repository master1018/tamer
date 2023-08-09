public final class nodehasattributes01 extends DOMTestCase {
   public nodehasattributes01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      NodeList elementList;
      boolean hasAttributes;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      element = (Element) elementList.item(0);
      hasAttributes = element.hasAttributes();
      assertFalse("nodehasattributes01_1", hasAttributes);
elementList = doc.getElementsByTagName("address");
      element = (Element) elementList.item(0);
      hasAttributes = element.hasAttributes();
      assertTrue("nodehasattributes01_2", hasAttributes);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodehasattributes01.class, args);
   }
}
