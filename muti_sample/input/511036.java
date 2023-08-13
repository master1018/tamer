public final class hc_nodelistreturnfirstitem extends DOMTestCase {
   public hc_nodelistreturnfirstitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      Node child;
      String childName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      child = employeeList.item(0);
      childName = child.getNodeName();
      if (equals("#text", childName)) {
          assertEquals("nodeName_w_space", "#text", childName);
      } else {
          assertEqualsAutoCase("element", "nodeName_wo_space", "em", childName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelistreturnfirstitem.class, args);
   }
}
