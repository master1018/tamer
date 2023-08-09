public final class hc_attrappendchild6 extends DOMTestCase {
   public hc_attrappendchild6(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("hc_staff", true);
      titleAttr = doc.createAttribute("title");
      textNode = doc.createTextNode("Yesterday");
      retval = titleAttr.appendChild(textNode);
      value = titleAttr.getValue();
      assertEquals("attrValue", "Yesterday", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "Yesterday", value);
      value = retval.getNodeValue();
      assertEquals("retvalValue", "Yesterday", value);
      lastChild = titleAttr.getLastChild();
      value = lastChild.getNodeValue();
      assertEquals("lastChildValue", "Yesterday", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrappendchild6.class, args);
   }
}
