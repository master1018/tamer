public final class namednodemapsetnameditemns07 extends DOMTestCase {
   public namednodemapsetnameditemns07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      Element element;
      Attr attr;
      Node newNode;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(0);
      attributes = element.getAttributes();
      attr = (Attr) attributes.getNamedItemNS("http:
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      {
         boolean success = false;
         try {
            newNode = attributes.setNamedItemNS(attr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("namednodemapsetnameditemns07", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns07.class, args);
   }
}
