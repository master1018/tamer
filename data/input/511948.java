public final class nodeissupported01 extends DOMTestCase {
   public nodeissupported01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String version = "";
      String version1 = "1.0";
      String version2 = "2.0";
      String featureCore;
      String featureXML;
      boolean success;
      java.util.List featuresXML = new java.util.ArrayList();
      featuresXML.add("XML");
      featuresXML.add("xmL");
      java.util.List featuresCore = new java.util.ArrayList();
      featuresCore.add("Core");
      featuresCore.add("CORE");
      doc = (Document) load("staffNS", false);
      element = doc.getDocumentElement();
      for (int indexN10063 = 0; indexN10063 < featuresXML.size(); indexN10063++) {
          featureXML = (String) featuresXML.get(indexN10063);
    success = element.isSupported(featureXML, version);
      assertTrue("nodeissupported01_XML1", success);
      success = element.isSupported(featureXML, version1);
      assertTrue("nodeissupported01_XML2", success);
        }
      for (int indexN1007C = 0; indexN1007C < featuresCore.size(); indexN1007C++) {
          featureCore = (String) featuresCore.get(indexN1007C);
    success = element.isSupported(featureCore, version);
      assertTrue("nodeissupported01_Core1", success);
      success = element.isSupported(featureCore, version1);
      success = element.isSupported(featureCore, version2);
      assertTrue("nodeissupported01_Core3", success);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeissupported01.class, args);
   }
}
