public final class hc_attrsetvalue1 extends DOMTestCase {
   public hc_attrsetvalue1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node retval;
      Node firstChild;
      Node otherChild;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      firstChild = titleAttr.getFirstChild();
      assertNotNull("attrChildNotNull", firstChild);
      titleAttr.setValue("Tomorrow");
      firstChild.setNodeValue("impl reused node");
      value = titleAttr.getValue();
      assertEquals("attrValue", "Tomorrow", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "Tomorrow", value);
      firstChild = titleAttr.getLastChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "Tomorrow", value);
      otherChild = firstChild.getNextSibling();
      assertNull("nextSiblingIsNull", otherChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrsetvalue1.class, args);
   }
}
