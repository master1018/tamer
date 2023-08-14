public class PageableDoc implements Doc {
    private Pageable pageable;
    public PageableDoc(Pageable pageable) {
       this.pageable = pageable;
    }
   public DocFlavor getDocFlavor() {
       return DocFlavor.SERVICE_FORMATTED.PAGEABLE;
   }
   public DocAttributeSet getAttributes() {
       return new HashDocAttributeSet();
   }
   public Object getPrintData() throws IOException {
      return pageable;
   }
   public Reader getReaderForText()
      throws UnsupportedEncodingException, IOException {
      return null;
   }
   public InputStream getStreamForBytes() throws IOException {
      return null;
   }
}
