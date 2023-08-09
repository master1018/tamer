public final class hc_elementnormalize extends DOMTestCase {
   public hc_elementnormalize(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      NodeList elementList;
      Element testName;
      Node firstChild;
      String childValue;
      Text textNode;
      Node retNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("sup");
      testName = (Element) elementList.item(0);
      textNode = doc.createTextNode("");
      retNode = testName.appendChild(textNode);
      textNode = doc.createTextNode(",000");
      retNode = testName.appendChild(textNode);
      root = doc.getDocumentElement();
      root.normalize();
      elementList = doc.getElementsByTagName("sup");
      testName = (Element) elementList.item(0);
      firstChild = testName.getFirstChild();
      childValue = firstChild.getNodeValue();
      assertEquals("elementNormalizeAssert", "56,000,000", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementnormalize.class, args);
   }
}
