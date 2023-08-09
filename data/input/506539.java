public final class hc_nodetextnodeattribute extends DOMTestCase {
   public hc_nodetextnodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddr;
      Node textNode;
      NamedNodeMap attrList;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = elementList.item(0);
      textNode = testAddr.getFirstChild();
      attrList = textNode.getAttributes();
      assertNull("text_attributes_is_null", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodetextnodeattribute.class, args);
   }
}
