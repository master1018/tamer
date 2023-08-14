public final class hc_attrinsertbefore1 extends DOMTestCase {
   public hc_attrinsertbefore1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node lastChild;
      Node refChild = null;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = doc.createTextNode("terday");
      retval = titleAttr.insertBefore(textNode, refChild);
      value = titleAttr.getValue();
      assertEquals("attrValue", "Yesterday", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "Yesterday", value);
      value = retval.getNodeValue();
      assertEquals("retvalValue", "terday", value);
      firstChild = titleAttr.getFirstChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "Yes", value);
      lastChild = titleAttr.getLastChild();
      value = lastChild.getNodeValue();
      assertEquals("lastChildValue", "terday", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrinsertbefore1.class, args);
   }
}
