public final class hc_commentgetcomment extends DOMTestCase {
   public hc_commentgetcomment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node child;
      String childName;
      String childValue;
      int commentCount = 0;
      int childType;
      NamedNodeMap attributes;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getChildNodes();
      for (int indexN1005E = 0; indexN1005E < elementList.getLength(); indexN1005E++) {
          child = (Node) elementList.item(indexN1005E);
    childType = (int) child.getNodeType();
      if (equals(8, childType)) {
          childName = child.getNodeName();
      assertEquals("nodeName", "#comment", childName);
      childValue = child.getNodeValue();
      assertEquals("nodeValue", " This is comment number 1.", childValue);
      attributes = child.getAttributes();
      assertNull("attributes", attributes);
      commentCount += 1;
      }
      }
      assertTrue("atMostOneComment", (commentCount < 2));
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_commentgetcomment.class, args);
   }
}
