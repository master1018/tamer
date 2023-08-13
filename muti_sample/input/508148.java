public final class hc_namednodemapremovenameditem extends DOMTestCase {
   public hc_namednodemapremovenameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Attr newAttribute;
      Node testAddress;
      NamedNodeMap attributes;
      Attr streetAttr;
      boolean specified;
      Node removedNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = elementList.item(2);
      attributes = testAddress.getAttributes();
      removedNode = attributes.removeNamedItem("class");
      streetAttr = (Attr) attributes.getNamedItem("class");
      assertNull("isnull", streetAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapremovenameditem.class, args);
   }
}
