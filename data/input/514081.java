public final class elementassociatedattribute extends DOMTestCase {
   public elementassociatedattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr domesticAttr;
      boolean specified;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(0);
      attributes = testEmployee.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("domestic");
      specified = domesticAttr.getSpecified();
      assertTrue("domesticSpecified", specified);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementassociatedattribute.class, args);
   }
}
