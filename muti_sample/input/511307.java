public final class elementsetattributenodens03 extends DOMTestCase {
   public elementsetattributenodens03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element1;
      Element element2;
      Attr attribute;
      Attr newAttribute;
      NodeList elementList;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element1 = (Element) elementList.item(1);
      attribute = element1.getAttributeNodeNS(nullNS, "street");
      element2 = (Element) elementList.item(2);
      {
         boolean success = false;
         try {
            newAttribute = element2.setAttributeNodeNS(attribute);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("elementsetattributenodens03", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens03.class, args);
   }
}
