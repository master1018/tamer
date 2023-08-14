public final class hc_attrreplacechild1 extends DOMTestCase {
   public hc_attrreplacechild1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = doc.createTextNode("terday");
      firstChild = titleAttr.getFirstChild();
      assertNotNull("attrChildNotNull", firstChild);
      retval = titleAttr.replaceChild(textNode, firstChild);
      value = titleAttr.getValue();
      assertEquals("attrValue", "terday", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "terday", value);
      value = retval.getNodeValue();
      assertEquals("retvalValue", "Yes", value);
      firstChild = titleAttr.getFirstChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "terday", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrreplacechild1.class, args);
   }
}
