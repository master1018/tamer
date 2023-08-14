public final class hc_attrcreatedocumentfragment extends DOMTestCase {
   public hc_attrcreatedocumentfragment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      Element newOne;
      Node domesticNode;
      NamedNodeMap attributes;
      Attr attribute;
      String attrName;
      Node appendedChild;
      int langAttrCount = 0;
      doc = (Document) load("hc_staff", true);
      docFragment = doc.createDocumentFragment();
      newOne = doc.createElement("html");
      newOne.setAttribute("lang", "EN");
      appendedChild = docFragment.appendChild(newOne);
      domesticNode = docFragment.getFirstChild();
      attributes = domesticNode.getAttributes();
      for (int indexN10078 = 0; indexN10078 < attributes.getLength(); indexN10078++) {
          attribute = (Attr) attributes.item(indexN10078);
    attrName = attribute.getNodeName();
      if (equalsAutoCase("attribute", "lang", attrName)) {
          langAttrCount += 1;
      }
      }
      assertEquals("hasLangAttr", 1, langAttrCount);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrcreatedocumentfragment.class, args);
   }
}
