public final class attrgetownerelement05 extends DOMTestCase {
   public attrgetownerelement05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node element;
      Element ownerElement;
      Element parentElement;
      NodeList elementList;
      String ownerElementName;
      Attr attr;
      Node removedChild;
      NamedNodeMap nodeMap;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = elementList.item(1);
      parentElement = (Element) element.getParentNode();
      nodeMap = element.getAttributes();
      removedChild = parentElement.removeChild(element);
      attr = (Attr) nodeMap.getNamedItemNS(nullNS, "street");
      ownerElement = attr.getOwnerElement();
      ownerElementName = ownerElement.getNodeName();
      assertEquals("attrgetownerelement05", "address", ownerElementName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrgetownerelement05.class, args);
   }
}
