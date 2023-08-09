public final class removeAttributeNS02 extends DOMTestCase {
   public removeAttributeNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddr;
      Attr addrAttr;
      String attr;
      String namespaceURI;
      String localName;
      String prefix;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      ((Element) testAddr).removeAttributeNS("http:
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      addrAttr = ((Element) testAddr).getAttributeNodeNS("http:
      attr = ((Element) testAddr).getAttributeNS("http:
      namespaceURI = addrAttr.getNamespaceURI();
      localName = addrAttr.getLocalName();
      prefix = testAddr.getPrefix();
      assertEquals("attr", "FALSE", attr);
      assertEquals("uri", "http:
      assertEquals("lname", "local1", localName);
      assertEquals("prefix", "emp", prefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(removeAttributeNS02.class, args);
   }
}
