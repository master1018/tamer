public final class attrcreatedocumentfragment extends DOMTestCase {
   public attrcreatedocumentfragment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      Element newOne;
      Node domesticNode;
      NamedNodeMap domesticAttr;
      Attr attrs;
      String attrName;
      Node appendedChild;
      doc = (Document) load("staff", true);
      docFragment = doc.createDocumentFragment();
      newOne = doc.createElement("newElement");
      newOne.setAttribute("newdomestic", "Yes");
      appendedChild = docFragment.appendChild(newOne);
      domesticNode = docFragment.getFirstChild();
      domesticAttr = domesticNode.getAttributes();
      attrs = (Attr) domesticAttr.item(0);
      attrName = attrs.getName();
      assertEquals("attrCreateDocumentFragmentAssert", "newdomestic", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrcreatedocumentfragment.class, args);
   }
}
