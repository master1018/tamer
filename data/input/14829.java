public class PrintAutoSenseData
{
  private DocFlavor flavor = DocFlavor.URL.AUTOSENSE; 
  private PrintService[] service = PrintServiceLookup.lookupPrintServices(flavor, null);
  public PrintAutoSenseData()
  {
     if (service.length == 0)
     {
        System.out.println("No print service available...");
        return;
     }
     System.out.println("selected PrintService: " + this.service[0]);
     if (service[0].isDocFlavorSupported(flavor)) {
         System.out.println("DocFlavor.URL.AUTOSENSE supported");
     } else {
         System.out.println("DocFlavor.URL.AUTOSENSE not supported. Testing aborted !!");
         return;
     }
     DocPrintJob job = service[0].createPrintJob();
     this.print();
  }
  void print()
  {
         String fileName = "./sample.txt";
         DocPrintJob job = service[0].createPrintJob();
         System.out.println("printing " + fileName + " using doc flavor: " + this.flavor);
         System.out.println("Rep. class name: " + this.flavor.getRepresentationClassName() + " MimeType: " + this.flavor.getMimeType());
         Doc doc = new URLDoc(fileName, this.flavor);
         HashPrintRequestAttributeSet prSet =
             new HashPrintRequestAttributeSet();
         prSet.add(new Destination(new File("./dest.prn").toURI()));
         try {
            job.print(doc, prSet);
         } catch ( Exception e ) {
            e.printStackTrace();
         }
  }
  public static void main(String[] args) {
     new PrintAutoSenseData();
  }
}
class URLDoc implements Doc
{
   protected String fileName = null;
   protected DocFlavor flavor = null;
   protected Object printData = null;
   protected InputStream instream = null;
   public URLDoc(String filename, DocFlavor docFlavor)
   {
      this.fileName = filename;
      this.flavor = docFlavor;
   }
   public DocFlavor getDocFlavor() {
       return DocFlavor.URL.AUTOSENSE;
   }
   public DocAttributeSet getAttributes()
   {
       HashDocAttributeSet hset = new HashDocAttributeSet();
       return hset;
   }
   public Object getPrintData()
   {
     if ( this.printData == null )
     {
        this.printData = URLDoc.class.getResource(this.fileName);
        System.out.println("getPrintData(): " + this.printData);
     }
     return this.printData;
   }
   public Reader getReaderForText()
   {
     return null;
   }
   public InputStream getStreamForBytes()
   {
     System.out.println("getStreamForBytes(): " + this.printData);
     try
     {
        if ( (this.printData != null) && (this.printData instanceof URL) )
        {
           this.instream = ((URL)this.printData).openStream();
        }
        if (this.instream == null)
        {
           URL url = URLDoc.class.getResource(this.fileName);
           this.instream = url.openStream();
        }
      }
      catch ( IOException ie )
      {
         System.out.println("URLDoc: exception: " + ie.toString());
      }
      return this.instream;
   }
}
