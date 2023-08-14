public final class hc_nodereplacechildnewchildexists extends DOMTestCase {
   public hc_nodereplacechildnewchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild = null;
      Node newChild = null;
      Node child;
      String childName;
      Node childNode;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("em");
      Node replacedChild;
      int nodeType;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = ((Element) employeeNode).getElementsByTagName("*");
      newChild = childList.item(0);
      oldChild = childList.item(5);
      replacedChild = employeeNode.replaceChild(newChild, oldChild);
      assertSame("return_value_same", oldChild, replacedChild);
for (int indexN10094 = 0; indexN10094 < childList.getLength(); indexN10094++) {
          childNode = (Node) childList.item(indexN10094);
    childName = childNode.getNodeName();
      nodeType = (int) childNode.getNodeType();
      if (equals(1, nodeType)) {
          actual.add(childName);
      } else {
          assertEquals("textNodeType", 3, nodeType);
      assertEquals("textNodeName", "#text", childName);
      }
      }
      assertEqualsAutoCase("element", "childNames", expected, actual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodereplacechildnewchildexists.class, args);
   }
}
