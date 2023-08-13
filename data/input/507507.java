public final class hc_documentcreateelement extends DOMTestCase {
   public hc_documentcreateelement(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element newElement;
      String newElementName;
      int newElementType;
      String newElementValue;
      doc = (Document) load("hc_staff", true);
      newElement = doc.createElement("acronym");
      newElementName = newElement.getNodeName();
      assertEqualsAutoCase("element", "strong", "acronym", newElementName);
        newElementType = (int) newElement.getNodeType();
      assertEquals("type", 1, newElementType);
      newElementValue = newElement.getNodeValue();
      assertNull("valueInitiallyNull", newElementValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentcreateelement.class, args);
   }
}
