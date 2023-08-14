public final class hc_nodeattributenodeattribute extends DOMTestCase {
   public hc_nodeattributenodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      NamedNodeMap addrAttr;
      Node attrNode;
      NamedNodeMap attrList;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributes();
      attrNode = addrAttr.item(0);
      attrList = attrNode.getAttributes();
      assertNull("nodeAttributeNodeAttributeAssert1", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeattributenodeattribute.class, args);
   }
}
