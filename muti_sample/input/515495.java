public final class hc_nodelistindexgetlengthofemptylist extends DOMTestCase {
   public hc_nodelistindexgetlengthofemptylist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList emList;
      Node emNode;
      Node textNode;
      NodeList textList;
      int length;
      doc = (Document) load("hc_staff", false);
      emList = doc.getElementsByTagName("em");
      emNode = emList.item(2);
      textNode = emNode.getFirstChild();
      textList = textNode.getChildNodes();
      length = (int) textList.getLength();
      assertEquals("length", 0, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelistindexgetlengthofemptylist.class, args);
   }
}
