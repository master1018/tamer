public final class hc_attrinsertbefore4 extends DOMTestCase {
   public hc_attrinsertbefore4(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node lastChild;
      Node refChild;
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
      refChild = titleAttr.getFirstChild();
      retval = titleAttr.insertBefore(docFrag, refChild);
      value = titleAttr.getValue();
      assertEquals("attrValue", "terdayYes", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "terdayYes", value);
      value = retval.getNodeValue();
      assertNull("retvalValue", value);
      firstChild = titleAttr.getFirstChild();
      value = firstChild.getNodeValue();
      assertEquals("firstChildValue", "ter", value);
      lastChild = titleAttr.getLastChild();
      value = lastChild.getNodeValue();
      assertEquals("lastChildValue", "Yes", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrinsertbefore4.class, args);
   }
}
