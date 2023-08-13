public final class prefix05 extends DOMTestCase {
   public prefix05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element addrNode;
      Attr addrAttr;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      addrNode = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", addrNode);
      addrAttr = addrNode.getAttributeNode("emp:domestic");
      {
         boolean success = false;
         try {
            addrAttr.setPrefix("xmlns");
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
        DOMTestCase.doMain(prefix05.class, args);
   }
}
