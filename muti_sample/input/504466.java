public final class hc_documentgetelementsbytagnamevalue extends DOMTestCase {
   public hc_documentgetelementsbytagnamevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      Node nameNode;
      Node firstChild;
      String childValue;
      doc = (Document) load("hc_staff", false);
      nameList = doc.getElementsByTagName("strong");
      nameNode = nameList.item(3);
      firstChild = nameNode.getFirstChild();
      childValue = firstChild.getNodeValue();
      assertEquals("documentGetElementsByTagNameValueAssert", "Jeny Oconnor", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetelementsbytagnamevalue.class, args);
   }
}
