public final class hc_nodeappendchildgetnodename extends DOMTestCase {
   public hc_nodeappendchildgetnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node newChild;
      Node appendNode;
      String childName;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newChild = doc.createElement("br");
      appendNode = employeeNode.appendChild(newChild);
      childName = appendNode.getNodeName();
      assertEqualsAutoCase("element", "nodeName", "br", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeappendchildgetnodename.class, args);
   }
}
