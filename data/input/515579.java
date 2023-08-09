public final class elementnormalize extends DOMTestCase {
   public elementnormalize(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      NodeList elementList;
      Element testName;
      Node firstChild;
      String childValue;
      doc = (Document) load("staff", true);
      root = doc.getDocumentElement();
      root.normalize();
      elementList = root.getElementsByTagName("name");
      testName = (Element) elementList.item(2);
      firstChild = testName.getFirstChild();
      childValue = firstChild.getNodeValue();
      assertEquals("elementNormalizeAssert", "Roger\n Jones", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementnormalize.class, args);
   }
}
