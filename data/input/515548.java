public final class attrgetownerelement03 extends DOMTestCase {
   public attrgetownerelement03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node ownerElement;
      Attr attr;
      doc = (Document) load("staffNS", false);
      attr = doc.createAttributeNS("http:
      ownerElement = attr.getOwnerElement();
      assertNull("attrgetownerelement03", ownerElement);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrgetownerelement03.class, args);
   }
}
