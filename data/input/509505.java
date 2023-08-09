public final class attrgetownerelement04 extends DOMTestCase {
   public attrgetownerelement04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docImp;
      Node ownerElement;
      Element element;
      Attr attr;
      Attr attrImp;
      NodeList addresses;
      doc = (Document) load("staffNS", false);
      docImp = (Document) load("staff", false);
      addresses = doc.getElementsByTagNameNS("http:
      element = (Element) addresses.item(1);
      assertNotNull("empAddressNotNull", element);
      attr = element.getAttributeNodeNS("http:
      attrImp = (Attr) docImp.importNode(attr, true);
      ownerElement = attrImp.getOwnerElement();
      assertNull("attrgetownerelement04", ownerElement);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrgetownerelement04.class, args);
   }
}
