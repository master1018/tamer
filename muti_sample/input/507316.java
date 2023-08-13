public final class hc_nodeelementnodeattributes extends DOMTestCase {
   public hc_nodeelementnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String attrName;
      java.util.Collection attrList = new java.util.ArrayList();
      java.util.Collection htmlExpected = new java.util.ArrayList();
      htmlExpected.add("title");
      htmlExpected.add("class");
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("title");
      expected.add("class");
      expected.add("dir");
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(2);
      addrAttr = testAddr.getAttributes();
      for (int indexN10070 = 0; indexN10070 < addrAttr.getLength(); indexN10070++) {
          attrNode = (Node) addrAttr.item(indexN10070);
    attrName = attrNode.getNodeName();
      attrList.add(attrName);
        }
      if (("text/html".equals(getContentType()))) {
          assertEqualsIgnoreCase("attrNames_html", htmlExpected, attrList);
} else {
          assertEquals("attrNames", expected, attrList);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeelementnodeattributes.class, args);
   }
}
