public final class nodehasattributes03 extends DOMTestCase {
   public nodehasattributes03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      NodeList elementList;
      boolean hasAttributes;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:employee");
      element = (Element) elementList.item(0);
      assertNotNull("empEmployeeNotNull", element);
      hasAttributes = element.hasAttributes();
      assertTrue("hasAttributes", hasAttributes);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodehasattributes03.class, args);
   }
}
