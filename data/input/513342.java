public final class namednodemapremovenameditemns08 extends DOMTestCase {
   public namednodemapremovenameditemns08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element element;
      Attr attribute;
      NodeList elementList;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      element.removeAttributeNS("http:
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
        DOMTestCase.doMain(namednodemapremovenameditemns08.class, args);
   }
}
