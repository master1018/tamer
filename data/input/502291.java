public final class domimplementationhasfeature01 extends DOMTestCase {
   public domimplementationhasfeature01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
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
      domImpl = doc.getImplementation();
      for (int indexN10063 = 0; indexN10063 < featuresXML.size(); indexN10063++) {
          featureXML = (String) featuresXML.get(indexN10063);
    success = domImpl.hasFeature(featureXML, version);
assertTrue("domimplementationhasfeature01_XML_1", success);
      success = domImpl.hasFeature(featureXML, version1);
assertTrue("domimplementationhasfeature01_XML_2", success);
        }
      for (int indexN1007C = 0; indexN1007C < featuresCore.size(); indexN1007C++) {
          featureCore = (String) featuresCore.get(indexN1007C);
    success = domImpl.hasFeature(featureCore, version);
assertTrue("domimplementationhasfeature01_Core_1", success);
      success = domImpl.hasFeature(featureCore, version1);
success = domImpl.hasFeature(featureCore, version2);
assertTrue("domimplementationhasfeature01_Core_3", success);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationhasfeature01.class, args);
   }
}
