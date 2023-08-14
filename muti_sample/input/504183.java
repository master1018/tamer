public final class createDocumentType02 extends DOMTestCase {
   public createDocumentType02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String publicId = "http:
      String systemId = "myDoc.dtd";
      String qualifiedName;
      Document doc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      java.util.List illegalQNames = new java.util.ArrayList();
      illegalQNames.add("edi:{");
      illegalQNames.add("edi:}");
      illegalQNames.add("edi:~");
      illegalQNames.add("edi:'");
      illegalQNames.add("edi:!");
      illegalQNames.add("edi:@");
      illegalQNames.add("edi:#");
      illegalQNames.add("edi:$");
      illegalQNames.add("edi:%");
      illegalQNames.add("edi:^");
      illegalQNames.add("edi:&");
      illegalQNames.add("edi:*");
      illegalQNames.add("edi:(");
      illegalQNames.add("edi:)");
      illegalQNames.add("edi:+");
      illegalQNames.add("edi:=");
      illegalQNames.add("edi:[");
      illegalQNames.add("edi:]");
      illegalQNames.add("edi:\\");
      illegalQNames.add("edi:/");
      illegalQNames.add("edi:;");
      illegalQNames.add("edi:`");
      illegalQNames.add("edi:<");
      illegalQNames.add("edi:>");
      illegalQNames.add("edi:,");
      illegalQNames.add("edi:a ");
      illegalQNames.add("edi:\"");
      doc = (Document) load("staffNS", false);
      for (int indexN1009A = 0; indexN1009A < illegalQNames.size(); indexN1009A++) {
          qualifiedName = (String) illegalQNames.get(indexN1009A);
    domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            docType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
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
        DOMTestCase.doMain(createDocumentType02.class, args);
   }
}
