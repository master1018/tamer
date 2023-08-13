public final class hc_nodeappendchildchildexists extends DOMTestCase {
   public hc_nodeappendchildchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      NodeList childList;
      Node childNode;
      Node newChild;
      Node memberNode;
      String memberName;
      java.util.List refreshedActual = new java.util.ArrayList();
      java.util.List actual = new java.util.ArrayList();
      int nodeType;
      java.util.List expected = new java.util.ArrayList();
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      expected.add("em");
      Node appendedChild;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      childNode = elementList.item(1);
      childList = ((Element) childNode).getElementsByTagName("*");
      newChild = childList.item(0);
      appendedChild = childNode.appendChild(newChild);
      for (int indexN10085 = 0; indexN10085 < childList.getLength(); indexN10085++) {
          memberNode = (Node) childList.item(indexN10085);
    memberName = memberNode.getNodeName();
      actual.add(memberName);
        }
      assertEqualsAutoCase("element", "liveByTagName", expected, actual);
        childList = childNode.getChildNodes();
      for (int indexN1009C = 0; indexN1009C < childList.getLength(); indexN1009C++) {
          memberNode = (Node) childList.item(indexN1009C);
    nodeType = (int) memberNode.getNodeType();
      if (equals(1, nodeType)) {
          memberName = memberNode.getNodeName();
      refreshedActual.add(memberName);
      }
      }
      assertEqualsAutoCase("element", "refreshedChildNodes", expected, refreshedActual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeappendchildchildexists.class, args);
   }
}
