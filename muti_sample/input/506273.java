public final class createAttributeNS03 extends DOMTestCase {
   public createAttributeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName;
      Document doc;
      Attr newAttr;
      java.util.List illegalQNames = new java.util.ArrayList();
      illegalQNames.add("person:{");
      illegalQNames.add("person:}");
      illegalQNames.add("person:~");
      illegalQNames.add("person:'");
      illegalQNames.add("person:!");
      illegalQNames.add("person:@");
      illegalQNames.add("person:#");
      illegalQNames.add("person:$");
      illegalQNames.add("person:%");
      illegalQNames.add("person:^");
      illegalQNames.add("person:&");
      illegalQNames.add("person:*");
      illegalQNames.add("person:(");
      illegalQNames.add("person:)");
      illegalQNames.add("person:+");
      illegalQNames.add("person:=");
      illegalQNames.add("person:[");
      illegalQNames.add("person:]");
      illegalQNames.add("person:\\");
      illegalQNames.add("person:/");
      illegalQNames.add("person:;");
      illegalQNames.add("person:`");
      illegalQNames.add("person:<");
      illegalQNames.add("person:>");
      illegalQNames.add("person:,");
      illegalQNames.add("person:a ");
      illegalQNames.add("person:\"");
      doc = (Document) load("staffNS", false);
      for (int indexN10090 = 0; indexN10090 < illegalQNames.size(); indexN10090++) {
          qualifiedName = (String) illegalQNames.get(indexN10090);
      {
         boolean success = false;
         try {
            newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("throw_INVALID_CHARACTER_ERR", success);
      }
  }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createAttributeNS03.class, args);
   }
}
