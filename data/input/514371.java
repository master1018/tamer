public final class hc_nodereplacechildnodename extends DOMTestCase {
   public hc_nodereplacechildnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild;
      Node newChild;
      Node replacedNode;
      String childName;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = ((Element) employeeNode).getElementsByTagName("em");
      oldChild = childList.item(0);
      newChild = doc.createElement("br");
      replacedNode = employeeNode.replaceChild(newChild, oldChild);
      childName = replacedNode.getNodeName();
      assertEqualsAutoCase("element", "replacedNodeName", "em", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodereplacechildnodename.class, args);
   }
}
