public final class hc_elementgetattributenode extends DOMTestCase {
   public hc_elementgetattributenode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr domesticAttr;
      String nodeName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(0);
      domesticAttr = testEmployee.getAttributeNode("title");
      nodeName = domesticAttr.getNodeName();
      assertEqualsAutoCase("attribute", "nodeName", "title", nodeName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetattributenode.class, args);
   }
}
