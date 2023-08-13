public final class attrgetownerelement01 extends DOMTestCase {
   public attrgetownerelement01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attr;
      Element element;
      Element ownerElement;
      String ownerElementName;
      NodeList elementList;
      NamedNodeMap attributes;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      attr = (Attr) attributes.getNamedItemNS(nullNS, "defaultAttr");
      ownerElement = attr.getOwnerElement();
      ownerElementName = ownerElement.getNodeName();
      assertEquals("attrgetownerelement01", "emp:employee", ownerElementName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrgetownerelement01.class, args);
   }
}
