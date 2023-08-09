public final class hc_elementretrieveallattributes extends DOMTestCase {
   public hc_elementretrieveallattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testAddress;
      NamedNodeMap attributes;
      Attr attribute;
      String attributeName;
      java.util.Collection actual = new java.util.ArrayList();
      java.util.Collection htmlExpected = new java.util.ArrayList();
      htmlExpected.add("title");
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("title");
      expected.add("dir");
      doc = (Document) load("hc_staff", false);
      addressList = doc.getElementsByTagName("acronym");
      testAddress = addressList.item(0);
      attributes = testAddress.getAttributes();
      for (int indexN1006B = 0; indexN1006B < attributes.getLength(); indexN1006B++) {
          attribute = (Attr) attributes.item(indexN1006B);
    attributeName = attribute.getNodeName();
      actual.add(attributeName);
        }
      if (("text/html".equals(getContentType()))) {
          assertEqualsIgnoreCase("htmlAttributeNames", htmlExpected, actual);
} else {
          assertEqualsIgnoreCase("attributeNames", expected, actual);
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementretrieveallattributes.class, args);
   }
}
