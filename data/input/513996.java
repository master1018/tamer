public final class hc_namednodemapnumberofnodes extends DOMTestCase {
   public hc_namednodemapnumberofnodes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      int length;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(2);
      attributes = testEmployee.getAttributes();
      length = (int) attributes.getLength();
      if (("text/html".equals(getContentType()))) {
          assertEquals("htmlLength", 2, length);
      } else {
          assertEquals("length", 3, length);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapnumberofnodes.class, args);
   }
}
