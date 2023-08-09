public final class nodecloneattributescopied extends DOMTestCase {
   public nodecloneattributescopied(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
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
      java.util.Collection expectedResult = new java.util.ArrayList();
      expectedResult.add("domestic");
      expectedResult.add("street");
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      addressNode = elementList.item(1);
      clonedNode = addressNode.cloneNode(false);
      attributes = clonedNode.getAttributes();
      for (int indexN10065 = 0; indexN10065 < attributes.getLength(); indexN10065++) {
          attributeNode = (Node) attributes.item(indexN10065);
    attributeName = attributeNode.getNodeName();
      result.add(attributeName);
        }
      assertEquals("nodeCloneAttributesCopiedAssert1", expectedResult, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecloneattributescopied.class, args);
   }
}
