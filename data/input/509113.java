public final class hc_noderemovechildgetnodename extends DOMTestCase {
   public hc_noderemovechildgetnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node removedChild;
      String childName;
      String oldName;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      oldChild = childList.item(0);
      oldName = oldChild.getNodeName();
      removedChild = employeeNode.removeChild(oldChild);
      assertNotNull("notnull", removedChild);
      childName = removedChild.getNodeName();
      assertEquals("nodeName", oldName, childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_noderemovechildgetnodename.class, args);
   }
}
