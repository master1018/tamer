public final class hc_elementnormalize2 extends DOMTestCase {
   public hc_elementnormalize2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      NodeList elementList;
      Element element;
      Node firstChild;
      Node secondChild;
      String childValue;
      Text emptyText;
      Attr attrNode;
      Node retval;
      doc = (Document) load("hc_staff", true);
      root = doc.getDocumentElement();
      emptyText = doc.createTextNode("");
      elementList = root.getElementsByTagName("acronym");
      element = (Element) elementList.item(0);
      attrNode = element.getAttributeNode("title");
      retval = attrNode.appendChild(emptyText);
      element.normalize();
      attrNode = element.getAttributeNode("title");
      firstChild = attrNode.getFirstChild();
      childValue = firstChild.getNodeValue();
      assertEquals("firstChild", "Yes", childValue);
      secondChild = firstChild.getNextSibling();
      assertNull("secondChildNull", secondChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementnormalize2.class, args);
   }
}
