public final class createElementNS03 extends DOMTestCase {
   public createElementNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName;
      Document doc;
      boolean done;
      Element newElement;
      String charact;
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
      for (int indexN10098 = 0; indexN10098 < illegalQNames.size(); indexN10098++) {
          qualifiedName = (String) illegalQNames.get(indexN10098);
      {
         boolean success = false;
         try {
            newElement = doc.createElementNS(namespaceURI, qualifiedName);
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
        DOMTestCase.doMain(createElementNS03.class, args);
   }
}
