public final class hc_nodedocumentfragmentnodevalue extends DOMTestCase {
   public hc_nodedocumentfragmentnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      NamedNodeMap attrList;
      String value;
      doc = (Document) load("hc_staff", true);
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
        DOMTestCase.doMain(hc_nodedocumentfragmentnodevalue.class, args);
   }
}
