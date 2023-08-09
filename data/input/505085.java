public final class nodeissupported02 extends DOMTestCase {
   public nodeissupported02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr attribute;
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
      attribute = doc.createAttribute("TestAttr");
      for (int indexN10064 = 0; indexN10064 < featuresXML.size(); indexN10064++) {
          featureXML = (String) featuresXML.get(indexN10064);
    success = attribute.isSupported(featureXML, version);
      assertTrue("nodeissupported02_XML1", success);
      success = attribute.isSupported(featureXML, version1);
      assertTrue("nodeissupported02_XML2", success);
        }
      for (int indexN1007D = 0; indexN1007D < featuresCore.size(); indexN1007D++) {
          featureCore = (String) featuresCore.get(indexN1007D);
    success = attribute.isSupported(featureCore, version);
      assertTrue("nodeissupported02_Core1", success);
      success = attribute.isSupported(featureCore, version1);
      success = attribute.isSupported(featureCore, version2);
      assertTrue("nodeissupported02_Core3", success);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeissupported02.class, args);
   }
}
