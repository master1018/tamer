public final class isSupported12 extends DOMTestCase {
   public isSupported12(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      java.util.List features = new java.util.ArrayList();
      features.add("Core");
      features.add("XML");
      features.add("HTML");
      features.add("Views");
      features.add("StyleSheets");
      features.add("CSS");
      features.add("CSS2");
      features.add("Events");
      features.add("UIEvents");
      features.add("MouseEvents");
      features.add("MutationEvents");
      features.add("HTMLEvents");
      features.add("Range");
      features.add("Traversal");
      features.add("bogus.bogus.bogus");
      Document doc;
      Node rootNode;
      String featureElement;
      boolean state;
      doc = (Document) load("staff", false);
      rootNode = doc.getDocumentElement();
      state = rootNode.isSupported("Core", "2.0");
      assertTrue("Core2", state);
      for (int indexN10078 = 0; indexN10078 < features.size(); indexN10078++) {
          featureElement = (String) features.get(indexN10078);
    state = rootNode.isSupported(featureElement, "1.0");
        }
      for (int indexN10083 = 0; indexN10083 < features.size(); indexN10083++) {
          featureElement = (String) features.get(indexN10083);
    state = rootNode.isSupported(featureElement, "2.0");
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(isSupported12.class, args);
   }
}
