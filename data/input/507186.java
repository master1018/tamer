public final class hc_nodegetlastchildnull extends DOMTestCase {
   public hc_nodegetlastchildnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList emList;
      Node emNode;
      CharacterData emText;
      Node nullChild;
      doc = (Document) load("hc_staff", false);
      emList = doc.getElementsByTagName("em");
      emNode = emList.item(0);
      emText = (CharacterData) emNode.getFirstChild();
      nullChild = emText.getLastChild();
      assertNull("nullChild", nullChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetlastchildnull.class, args);
   }
}
