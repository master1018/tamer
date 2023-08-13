public final class hc_nodedocumentfragmentnormalize2 extends DOMTestCase {
   public hc_nodedocumentfragmentnormalize2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      String nodeValue;
      Text txtNode;
      Node retval;
      doc = (Document) load("hc_staff", true);
      docFragment = doc.createDocumentFragment();
      txtNode = doc.createTextNode("");
      retval = docFragment.appendChild(txtNode);
      docFragment.normalize();
      txtNode = (Text) docFragment.getFirstChild();
      assertNull("noChild", txtNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentfragmentnormalize2.class, args);
   }
}
