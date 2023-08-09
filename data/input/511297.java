public final class hc_nodegetfirstchild extends DOMTestCase {
   public hc_nodegetfirstchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node fchildNode;
      String childName;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      fchildNode = employeeNode.getFirstChild();
      childName = fchildNode.getNodeName();
      if (equals("#text", childName)) {
          assertEquals("firstChild_w_whitespace", "#text", childName);
      } else {
          assertEqualsAutoCase("element", "firstChild_wo_whitespace", "em", childName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetfirstchild.class, args);
   }
}
