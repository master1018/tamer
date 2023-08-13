public final class createElementNS05 extends DOMTestCase {
   public createElementNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "gov:faculty";
      Document doc;
      Element newElement;
      String elementName;
      doc = (Document) load("staffNS", false);
      newElement = doc.createElementNS(namespaceURI, qualifiedName);
      elementName = newElement.getTagName();
      assertEquals("throw_Equals", qualifiedName, elementName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createElementNS05.class, args);
   }
}
