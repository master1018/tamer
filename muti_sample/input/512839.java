public final class elementretrieveattrvalue extends DOMTestCase {
   public elementretrieveattrvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      String attrValue;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddress = (Element) elementList.item(2);
      attrValue = testAddress.getAttribute("street");
      assertEquals("attrValue", "No", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementretrieveattrvalue.class, args);
   }
}
