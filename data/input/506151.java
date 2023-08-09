public final class nodesetprefix01 extends DOMTestCase {
   public nodesetprefix01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      Element element;
      String elementTagName;
      String elementNodeName;
      Node appendedChild;
      doc = (Document) load("staff", true);
      docFragment = doc.createDocumentFragment();
      element = doc.createElementNS("http:
      appendedChild = docFragment.appendChild(element);
      element.setPrefix("dmstc");
      elementTagName = element.getTagName();
      elementNodeName = element.getNodeName();
      assertEquals("nodesetprefix01_tagname", "dmstc:address", elementTagName);
      assertEquals("nodesetprefix01_nodeName", "dmstc:address", elementNodeName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodesetprefix01.class, args);
   }
}
