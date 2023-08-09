public abstract class PrinterJob {
    public static PrinterJob getPrinterJob() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        return (PrinterJob) java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
                String nm = System.getProperty("java.awt.printerjob", null);
                try {
                    return (PrinterJob)Class.forName(nm).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new AWTError("PrinterJob not found: " + nm);
                } catch (InstantiationException e) {
                 throw new AWTError("Could not instantiate PrinterJob: " + nm);
                } catch (IllegalAccessException e) {
                    throw new AWTError("Could not access PrinterJob: " + nm);
                }
            }
        });
    }
    public static PrintService[] lookupPrintServices() {
        return PrintServiceLookup.
            lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
    }
    public static StreamPrintServiceFactory[]
        lookupStreamPrintServices(String mimeType) {
        return StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
                                       DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                       mimeType);
    }
    public PrinterJob() {
    }
    public PrintService getPrintService() {
        return null;
    }
    public void setPrintService(PrintService service)
        throws PrinterException {
            throw new PrinterException(
                         "Setting a service is not supported on this class");
    }
    public abstract void setPrintable(Printable painter);
    public abstract void setPrintable(Printable painter, PageFormat format);
    public abstract void setPageable(Pageable document)
        throws NullPointerException;
    public abstract boolean printDialog() throws HeadlessException;
    public boolean printDialog(PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        return printDialog();
    }
    public abstract PageFormat pageDialog(PageFormat page)
        throws HeadlessException;
    public PageFormat pageDialog(PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        return pageDialog(defaultPage());
    }
    public abstract PageFormat defaultPage(PageFormat page);
    public PageFormat defaultPage() {
        return defaultPage(new PageFormat());
    }
    public PageFormat getPageFormat(PrintRequestAttributeSet attributes) {
        PrintService service = getPrintService();
        PageFormat pf = defaultPage();
        if (service == null || attributes == null) {
            return pf;
        }
        Media media = (Media)attributes.get(Media.class);
        MediaPrintableArea mpa =
            (MediaPrintableArea)attributes.get(MediaPrintableArea.class);
        OrientationRequested orientReq =
           (OrientationRequested)attributes.get(OrientationRequested.class);
        if (media == null && mpa == null && orientReq == null) {
           return pf;
        }
        Paper paper = pf.getPaper();
        if (mpa == null && media != null &&
            service.isAttributeCategorySupported(MediaPrintableArea.class)) {
            Object mpaVals =
                service.getSupportedAttributeValues(MediaPrintableArea.class,
                                                    null, attributes);
            if (mpaVals instanceof MediaPrintableArea[] &&
                ((MediaPrintableArea[])mpaVals).length > 0) {
                mpa = ((MediaPrintableArea[])mpaVals)[0];
            }
        }
        if (media != null &&
            service.isAttributeValueSupported(media, null, attributes)) {
            if (media instanceof MediaSizeName) {
                MediaSizeName msn = (MediaSizeName)media;
                MediaSize msz = MediaSize.getMediaSizeForName(msn);
                if (msz != null) {
                    double inch = 72.0;
                    double paperWid = msz.getX(MediaSize.INCH) * inch;
                    double paperHgt = msz.getY(MediaSize.INCH) * inch;
                    paper.setSize(paperWid, paperHgt);
                    if (mpa == null) {
                        paper.setImageableArea(inch, inch,
                                               paperWid-2*inch,
                                               paperHgt-2*inch);
                    }
                }
            }
        }
        if (mpa != null &&
            service.isAttributeValueSupported(mpa, null, attributes)) {
            float [] printableArea =
                mpa.getPrintableArea(MediaPrintableArea.INCH);
            for (int i=0; i < printableArea.length; i++) {
                printableArea[i] = printableArea[i]*72.0f;
            }
            paper.setImageableArea(printableArea[0], printableArea[1],
                                   printableArea[2], printableArea[3]);
        }
        if (orientReq != null &&
            service.isAttributeValueSupported(orientReq, null, attributes)) {
            int orient;
            if (orientReq.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
                orient = PageFormat.REVERSE_LANDSCAPE;
            } else if (orientReq.equals(OrientationRequested.LANDSCAPE)) {
                orient = PageFormat.LANDSCAPE;
            } else {
                orient = PageFormat.PORTRAIT;
            }
            pf.setOrientation(orient);
        }
        pf.setPaper(paper);
        pf = validatePage(pf);
        return pf;
    }
    public abstract PageFormat validatePage(PageFormat page);
    public abstract void print() throws PrinterException;
    public void print(PrintRequestAttributeSet attributes)
        throws PrinterException {
        print();
    }
    public abstract void setCopies(int copies);
    public abstract int getCopies();
    public abstract String getUserName();
    public abstract void setJobName(String jobName);
    public abstract String getJobName();
    public abstract void cancel();
    public abstract boolean isCancelled();
}
