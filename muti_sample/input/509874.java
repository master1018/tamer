public final class hc_elementretrieveattrvalue extends DOMTestCase {
   public hc_elementretrieveattrvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      String attrValue;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = (Element) elementList.item(2);
      attrValue = testAddress.getAttribute("class");
      assertEquals("attrValue", "No", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementretrieveattrvalue.class, args);
   }
}
