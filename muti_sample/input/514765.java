public final class hc_attrnormalize extends DOMTestCase {
   public hc_attrnormalize(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList acronymList;
      Node testNode;
      NamedNodeMap attributes;
      Attr titleAttr;
      String value;
      Text textNode;
      Node retval;
      Node firstChild;
      Node secondChild;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = doc.createTextNode("terday");
      retval = titleAttr.appendChild(textNode);
      textNode = doc.createTextNode("");
      retval = titleAttr.appendChild(textNode);
      ((Element) testNode).normalize();
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "Yesterday", value);
      firstChild = titleAttr.getFirstChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "Yesterday", value);
      secondChild = firstChild.getNextSibling();
      assertNull("secondChildIsNull", secondChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrnormalize.class, args);
   }
}
