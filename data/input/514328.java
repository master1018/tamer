public final class hc_namednodemapreturnlastitem extends DOMTestCase {
   public hc_namednodemapreturnlastitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Node child;
      String nodeName;
      java.util.Collection htmlExpected = new java.util.ArrayList();
      htmlExpected.add("title");
      htmlExpected.add("class");
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("title");
      expected.add("class");
      expected.add("dir");
      java.util.Collection actual = new java.util.ArrayList();
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      for (int indexN10070 = 0; indexN10070 < attributes.getLength(); indexN10070++) {
          child = (Node) attributes.item(indexN10070);
    nodeName = child.getNodeName();
      actual.add(nodeName);
        }
      if (("text/html".equals(getContentType()))) {
          assertEqualsIgnoreCase("attrName_html", htmlExpected, actual);
} else {
          assertEquals("attrName", expected, actual);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapreturnlastitem.class, args);
   }
}
