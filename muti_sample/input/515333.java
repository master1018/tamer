public final class nodedocumentfragmentnodevalue extends DOMTestCase {
   public nodedocumentfragmentnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      NamedNodeMap attrList;
      String value;
      doc = (Document) load("staff", true);
      docFragment = doc.createDocumentFragment();
      attrList = docFragment.getAttributes();
      assertNull("attributesNull", attrList);
      value = docFragment.getNodeValue();
      assertNull("initiallyNull", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentfragmentnodevalue.class, args);
   }
}
