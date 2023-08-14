public final class hc_elementassociatedattribute extends DOMTestCase {
   public hc_elementassociatedattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr domesticAttr;
      boolean specified;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(0);
      attributes = testEmployee.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("title");
      specified = domesticAttr.getSpecified();
      assertTrue("acronymTitleSpecified", specified);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementassociatedattribute.class, args);
   }
}
