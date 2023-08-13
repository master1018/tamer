public final class hc_attrclonenode1 extends DOMTestCase {
   public hc_attrclonenode1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node lastChild;
      Attr clonedTitle;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = doc.createTextNode("terday");
      retval = titleAttr.appendChild(textNode);
      clonedTitle = (Attr) titleAttr.cloneNode(false);
      textNode.setNodeValue("text_node_not_cloned");
      value = clonedTitle.getValue();
      assertEquals("attrValue", "Yesterday", value);
      value = clonedTitle.getNodeValue();
      assertEquals("attrNodeValue", "Yesterday", value);
      lastChild = clonedTitle.getLastChild();
      value = lastChild.getNodeValue();
      assertEquals("lastChildValue", "terday", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrclonenode1.class, args);
   }
}
