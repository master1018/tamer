public final class namednodemapremovenameditemns06 extends DOMTestCase {
   public namednodemapremovenameditemns06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap attributes;
      Node element;
      Attr attribute;
      NodeList elementList;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = elementList.item(1);
      attributes = element.getAttributes();
      {
         boolean success = false;
         try {
            attribute = (Attr) attributes.removeNamedItemNS("http:
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_FOUND_ERR);
         }
         assertTrue("throw_NOT_FOUND_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns06.class, args);
   }
}
