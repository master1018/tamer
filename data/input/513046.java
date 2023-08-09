public final class nodesetprefix08 extends DOMTestCase {
   public nodesetprefix08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      NodeList elementList;
      Attr attribute;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("employee");
      element = (Element) elementList.item(0);
      attribute = element.getAttributeNode("xmlns");
      {
         boolean success = false;
         try {
            attribute.setPrefix("xml");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("throw_NAMESPACE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodesetprefix08.class, args);
   }
}
