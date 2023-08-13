public final class hc_attrreplacechild2 extends DOMTestCase {
   public hc_attrreplacechild2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      DocumentFragment docFrag;
      Node retval;
      Node firstChild;
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
      firstChild = titleAttr.getFirstChild();
      assertNotNull("attrChildNotNull", firstChild);
      retval = titleAttr.replaceChild(docFrag, firstChild);
      value = titleAttr.getValue();
      assertEquals("attrValue", "terday", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "terday", value);
      value = retval.getNodeValue();
      assertEquals("retvalValue", "Yes", value);
      firstChild = titleAttr.getFirstChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "ter", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrreplacechild2.class, args);
   }
}
