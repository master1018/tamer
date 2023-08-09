public final class hc_textindexsizeerrnegativeoffset extends DOMTestCase {
   public hc_textindexsizeerrnegativeoffset(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.signed
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Text textNode;
      Text splitNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(2);
      textNode = (Text) nameNode.getFirstChild();
      {
         boolean success = false;
         try {
            splitNode = textNode.splitText(-69);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INDEX_SIZE_ERR);
         }
         assertTrue("throws_INDEX_SIZE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_textindexsizeerrnegativeoffset.class, args);
   }
}
