public final class nodesetprefix05 extends DOMTestCase {
   public nodesetprefix05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String prefixValue;
      java.util.List prefixValues = new java.util.ArrayList();
      prefixValues.add("_:");
      prefixValues.add(":0");
      prefixValues.add(":");
      prefixValues.add("_::");
      prefixValues.add("a:0:c");
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      for (int indexN10050 = 0; indexN10050 < prefixValues.size(); indexN10050++) {
          prefixValue = (String) prefixValues.get(indexN10050);
      {
         boolean success = false;
         try {
            element.setPrefix(prefixValue);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("throw_NAMESPACE_ERR", success);
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodesetprefix05.class, args);
   }
}
