public final class prefix09 extends DOMTestCase {
   public prefix09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      Element addrNode;
      Attr addrAttr;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("address");
      addrNode = (Element) elementList.item(3);
      addrAttr = addrNode.getAttributeNode("xmlns");
      {
         boolean success = false;
         try {
            addrAttr.setPrefix("xxx");
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
        DOMTestCase.doMain(prefix09.class, args);
   }
}
