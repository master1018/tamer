class StreamPrintingOrientation implements Printable {
         public StreamPrintingOrientation() {
                super();
        }
        public static void main(java.lang.String[] args) {
                StreamPrintingOrientation pd = new StreamPrintingOrientation();
                PrinterJob pj = PrinterJob.getPrinterJob();
                HashPrintRequestAttributeSet prSet = new HashPrintRequestAttributeSet();
                PrintService service = null;
                FileOutputStream fos = null;
                File f = null, f1 = null;
                String mType = "application/postscript";
                try {
                        f = new File("streamexample.ps");
                        fos = new FileOutputStream(f);
                        StreamPrintServiceFactory[] factories = PrinterJob.lookupStreamPrintServices(mType);
                        if (factories.length > 0)
                                service = factories[0].getPrintService(fos);
                        if (service != null) {
                                System.out.println("Stream Print Service "+service);
                                pj.setPrintService(service);
                        } else {
                                throw new RuntimeException("No stream Print Service available.");
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                pj.setPrintable(pd);
                prSet.add(OrientationRequested.LANDSCAPE);
                prSet.add(new Copies(3));
                prSet.add(new JobName("orientation test", null));
                System.out.println("open PrintDialog..");
                if (pj.printDialog(prSet)) {
                        try {
                                System.out.println("\nValues in attr set passed to print method");
                                Attribute attr[] = prSet.toArray();
                                for (int x = 0; x < attr.length; x ++) {
                                        System.out.println("Name "+attr[x].getName()+"  "+attr[x]);
                                }
                                System.out.println("About to print the data ...");
                                if (service != null) {
                                        System.out.println("TEST: calling Print");
                                        pj.print(prSet);
                                        System.out.println("TEST: Printed");
                                }
                        }
                        catch (PrinterException pe) {
                                pe.printStackTrace();
                        }
                }
        }
        public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
                if (pi > 0) {
                        return Printable.NO_SUCH_PAGE;
                }
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.black);
                g2.translate(pf.getImageableX(), pf.getImageableY());
                System.out.println("StreamPrinting Test Width "+pf.getWidth()+" Height "+pf.getHeight());
                g2.drawRect(1,1,200,300);
                g2.drawRect(1,1,25,25);
                return Printable.PAGE_EXISTS;
        }
}
