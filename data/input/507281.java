public final class hc_attrappendchild3 extends DOMTestCase {
   public hc_attrappendchild3(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Text terNode;
      Text dayNode;
      Node retval;
      Node lastChild;
      DocumentFragment docFrag;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      terNode = doc.createTextNode("ter");
      dayNode = doc.createTextNode("day");
      docFrag = doc.createDocumentFragment();
      retval = docFrag.appendChild(terNode);
      retval = docFrag.appendChild(dayNode);
      retval = titleAttr.appendChild(docFrag);
      value = titleAttr.getValue();
      assertEquals("attrValue", "Yesterday", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "Yesterday", value);
      value = retval.getNodeValue();
      assertNull("retvalValue", value);
      lastChild = titleAttr.getLastChild();
      value = lastChild.getNodeValue();
      assertEquals("lastChildValue", "day", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrappendchild3.class, args);
   }
}
