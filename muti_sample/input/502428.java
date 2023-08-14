public final class hc_namednodemapchildnoderange extends DOMTestCase {
   public hc_namednodemapchildnoderange(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Node child;
      String strong;
      int length;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(2);
      attributes = testEmployee.getAttributes();
      length = (int) attributes.getLength();
      if (("text/html".equals(getContentType()))) {
          assertEquals("htmlLength", 2, length);
      } else {
          assertEquals("length", 3, length);
      child = attributes.item(2);
      assertNotNull("attr2", child);
      }
    child = attributes.item(0);
      assertNotNull("attr0", child);
      child = attributes.item(1);
      assertNotNull("attr1", child);
      child = attributes.item(3);
      assertNull("attr3", child);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapchildnoderange.class, args);
   }
}
