public final class createDocument05 extends DOMTestCase {
   public createDocument05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName;
      Document doc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Document aNewDoc;
      String charact;
      java.util.List illegalQNames = new java.util.ArrayList();
      illegalQNames.add("namespaceURI:{");
      illegalQNames.add("namespaceURI:}");
      illegalQNames.add("namespaceURI:~");
      illegalQNames.add("namespaceURI:'");
      illegalQNames.add("namespaceURI:!");
      illegalQNames.add("namespaceURI:@");
      illegalQNames.add("namespaceURI:#");
      illegalQNames.add("namespaceURI:$");
      illegalQNames.add("namespaceURI:%");
      illegalQNames.add("namespaceURI:^");
      illegalQNames.add("namespaceURI:&");
      illegalQNames.add("namespaceURI:*");
      illegalQNames.add("namespaceURI:(");
      illegalQNames.add("namespaceURI:)");
      illegalQNames.add("namespaceURI:+");
      illegalQNames.add("namespaceURI:=");
      illegalQNames.add("namespaceURI:[");
      illegalQNames.add("namespaceURI:]");
      illegalQNames.add("namespaceURI:\\");
      illegalQNames.add("namespaceURI:/");
      illegalQNames.add("namespaceURI:;");
      illegalQNames.add("namespaceURI:`");
      illegalQNames.add("namespaceURI:<");
      illegalQNames.add("namespaceURI:>");
      illegalQNames.add("namespaceURI:,");
      illegalQNames.add("namespaceURI:a ");
      illegalQNames.add("namespaceURI:\"");
      doc = (Document) load("staffNS", false);
      for (int indexN1009A = 0; indexN1009A < illegalQNames.size(); indexN1009A++) {
          qualifiedName = (String) illegalQNames.get(indexN1009A);
    domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            aNewDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
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
        DOMTestCase.doMain(createDocument05.class, args);
   }
}
