public final class nodelistindexgetlengthofemptylist extends DOMTestCase {
   public nodelistindexgetlengthofemptylist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      Node childNode;
      Node textNode;
      NodeList textList;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      childNode = employeeList.item(1);
      textNode = childNode.getFirstChild();
      textList = textNode.getChildNodes();
      assertSize("nodelistIndexGetLengthOfEmptyListAssert", 0, textList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodelistindexgetlengthofemptylist.class, args);
   }
}
