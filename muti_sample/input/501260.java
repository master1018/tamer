public final class hc_nodedocumentnodeattribute extends DOMTestCase {
   public hc_nodedocumentnodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap attrList;
      doc = (Document) load("hc_staff", false);
      attrList = doc.getAttributes();
      assertNull("doc_attributes_is_null", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentnodeattribute.class, args);
   }
}
