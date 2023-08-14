public final class hc_nodecloneattributescopied extends DOMTestCase {
   public hc_nodecloneattributescopied(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node addressNode;
      Node clonedNode;
      NamedNodeMap attributes;
      Node attributeNode;
      String attributeName;
      java.util.Collection result = new java.util.ArrayList();
      java.util.Collection htmlExpected = new java.util.ArrayList();
      htmlExpected.add("class");
      htmlExpected.add("title");
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("class");
      expected.add("title");
      expected.add("dir");
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      addressNode = elementList.item(1);
      clonedNode = addressNode.cloneNode(false);
      attributes = clonedNode.getAttributes();
      for (int indexN10076 = 0; indexN10076 < attributes.getLength(); indexN10076++) {
          attributeNode = (Node) attributes.item(indexN10076);
    attributeName = attributeNode.getNodeName();
      result.add(attributeName);
        }
      if (("text/html".equals(getContentType()))) {
          assertEqualsIgnoreCase("nodeNames_html", htmlExpected, result);
} else {
          assertEquals("nodeNames", expected, result);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodecloneattributescopied.class, args);
   }
}
