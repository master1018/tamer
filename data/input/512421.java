public final class attrgetownerelement02 extends DOMTestCase {
   public attrgetownerelement02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      Element ownerElement;
      String ownerElementName;
      Attr attr;
      Attr newAttr;
      doc = (Document) load("staffNS", false);
      element = doc.createElement("root");
      attr = doc.createAttributeNS("http:
      newAttr = element.setAttributeNodeNS(attr);
      ownerElement = attr.getOwnerElement();
      ownerElementName = ownerElement.getNodeName();
      assertEqualsIgnoreCase("attrgetownerelement02", "root", ownerElementName);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrgetownerelement02.class, args);
   }
}
