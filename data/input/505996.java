public final class namednodemapsetnameditemns03 extends DOMTestCase {
   public namednodemapsetnameditemns03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docAlt;
      NamedNodeMap attributes;
      NamedNodeMap attributesAlt;
      NodeList elementList;
      NodeList elementListAlt;
      Element element;
      Element elementAlt;
      Attr attr;
      Node newNode;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      docAlt = (Document) load("staffNS", true);
      elementListAlt = docAlt.getElementsByTagNameNS("*", "address");
      elementAlt = (Element) elementListAlt.item(1);
      attributesAlt = elementAlt.getAttributes();
      attr = (Attr) attributesAlt.getNamedItemNS(nullNS, "street");
      newNode = attributesAlt.removeNamedItemNS(nullNS, "street");
      {
         boolean success = false;
         try {
            newNode = attributes.setNamedItemNS(attr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
         }
         assertTrue("throw_WRONG_DOCUMENT_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns03.class, args);
   }
}
