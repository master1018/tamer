public final class documentcreateelement extends DOMTestCase {
   public documentcreateelement(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element newElement;
      String newElementName;
      int newElementType;
      String newElementValue;
      doc = (Document) load("staff", true);
      newElement = doc.createElement("address");
      newElementName = newElement.getNodeName();
      assertEquals("name", "address", newElementName);
      newElementType = (int) newElement.getNodeType();
      assertEquals("type", 1, newElementType);
      newElementValue = newElement.getNodeValue();
      assertNull("valueInitiallyNull", newElementValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelement.class, args);
   }
}
