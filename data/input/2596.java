public abstract class RasterPrinterJob extends PrinterJob {
    protected static final int PRINTER = 0;
    protected static final int FILE = 1;
    protected static final int STREAM = 2;
    private static final int MAX_BAND_SIZE = (1024 * 1024 * 4);
    private static final float DPI = 72.0f;
    private static final String FORCE_PIPE_PROP = "sun.java2d.print.pipeline";
    private static final String FORCE_RASTER = "raster";
    private static final String FORCE_PDL = "pdl";
    private static final String SHAPE_TEXT_PROP = "sun.java2d.print.shapetext";
    public static boolean forcePDL = false;
    public static boolean forceRaster = false;
    public static boolean shapeTextProp = false;
    static {
        String forceStr =
           (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction(FORCE_PIPE_PROP));
        if (forceStr != null) {
            if (forceStr.equalsIgnoreCase(FORCE_PDL)) {
                forcePDL = true;
            } else if (forceStr.equalsIgnoreCase(FORCE_RASTER)) {
                forceRaster = true;
            }
        }
        String shapeTextStr =
           (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction(SHAPE_TEXT_PROP));
        if (shapeTextStr != null) {
            shapeTextProp = true;
        }
    }
    private int cachedBandWidth = 0;
    private int cachedBandHeight = 0;
    private BufferedImage cachedBand = null;
    private int mNumCopies = 1;
    private boolean mCollate = false;
    private int mFirstPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
    private int mLastPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
    private Paper previousPaper;
    private Pageable mDocument = new Book();
    private String mDocName = "Java Printing";
    private boolean performingPrinting = false;
    private boolean userCancelled = false;
    private FilePermission printToFilePermission;
    private ArrayList redrawList = new ArrayList();
    private int copiesAttr;
    private String jobNameAttr;
    private String userNameAttr;
    private PageRanges pageRangesAttr;
    protected Sides sidesAttr;
    protected String destinationAttr;
    protected boolean noJobSheet = false;
    protected int mDestType = RasterPrinterJob.FILE;
    protected String mDestination = "";
    protected boolean collateAttReq = false;
    protected boolean landscapeRotates270 = false;
    protected PrintRequestAttributeSet attributes = null;
    private class GraphicsState {
        Rectangle2D region;  
        Shape theClip;       
        AffineTransform theTransform; 
        double sx;           
        double sy;           
    }
    protected PrintService myService;
    public RasterPrinterJob()
    {
    }
    abstract protected double getXRes();
    abstract protected double getYRes();
    abstract protected double getPhysicalPrintableX(Paper p);
    abstract protected double getPhysicalPrintableY(Paper p);
    abstract protected double getPhysicalPrintableWidth(Paper p);
    abstract protected double getPhysicalPrintableHeight(Paper p);
    abstract protected double getPhysicalPageWidth(Paper p);
    abstract protected double getPhysicalPageHeight(Paper p);
    abstract protected void startPage(PageFormat format, Printable painter,
                                      int index, boolean paperChanged)
        throws PrinterException;
    abstract protected void endPage(PageFormat format, Printable painter,
                                    int index)
        throws PrinterException;
    abstract protected void printBand(byte[] data, int x, int y,
                                      int width, int height)
        throws PrinterException;
    public void saveState(AffineTransform at, Shape clip,
                          Rectangle2D region, double sx, double sy) {
        GraphicsState gstate = new GraphicsState();
        gstate.theTransform = at;
        gstate.theClip = clip;
        gstate.region = region;
        gstate.sx = sx;
        gstate.sy = sy;
        redrawList.add(gstate);
    }
    protected static PrintService lookupDefaultPrintService() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service != null &&
            service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PAGEABLE) &&
            service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
            return service;
        } else {
           PrintService []services =
             PrintServiceLookup.lookupPrintServices(
                                DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
           if (services.length > 0) {
               return services[0];
           }
        }
        return null;
    }
    public PrintService getPrintService() {
        if (myService == null) {
            PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
            if (svc != null &&
                svc.isDocFlavorSupported(
                     DocFlavor.SERVICE_FORMATTED.PAGEABLE)) {
                try {
                    setPrintService(svc);
                    myService = svc;
                } catch (PrinterException e) {
                }
            }
            if (myService == null) {
                PrintService[] svcs = PrintServiceLookup.lookupPrintServices(
                    DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
                if (svcs.length > 0) {
                    try {
                        setPrintService(svcs[0]);
                        myService = svcs[0];
                    } catch (PrinterException e) {
                    }
                }
            }
        }
        return myService;
    }
    public void setPrintService(PrintService service)
        throws PrinterException {
        if (service == null) {
            throw new PrinterException("Service cannot be null");
        } else if (!(service instanceof StreamPrintService) &&
                   service.getName() == null) {
            throw new PrinterException("Null PrintService name.");
        } else {
            PrinterState prnState = (PrinterState)service.getAttribute(
                                                  PrinterState.class);
            if (prnState == PrinterState.STOPPED) {
                PrinterStateReasons prnStateReasons =
                    (PrinterStateReasons)service.getAttribute(
                                                 PrinterStateReasons.class);
                if ((prnStateReasons != null) &&
                    (prnStateReasons.containsKey(PrinterStateReason.SHUTDOWN)))
                {
                    throw new PrinterException("PrintService is no longer available.");
                }
            }
            if (service.isDocFlavorSupported(
                                             DocFlavor.SERVICE_FORMATTED.PAGEABLE) &&
                service.isDocFlavorSupported(
                                             DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                myService = service;
            } else {
                throw new PrinterException("Not a 2D print service: " + service);
            }
        }
    }
    protected void updatePageAttributes(PrintService service,
                                        PageFormat page) {
        if (service == null || page == null) {
            return;
        }
        float x = (float)Math.rint(
                         (page.getPaper().getWidth()*Size2DSyntax.INCH)/
                         (72.0))/(float)Size2DSyntax.INCH;
        float y = (float)Math.rint(
                         (page.getPaper().getHeight()*Size2DSyntax.INCH)/
                         (72.0))/(float)Size2DSyntax.INCH;
        Media[] mediaList = (Media[])service.getSupportedAttributeValues(
                                      Media.class, null, null);
        Media media = null;
        try {
            media = CustomMediaSizeName.findMedia(mediaList, x, y,
                                   Size2DSyntax.INCH);
        } catch (IllegalArgumentException iae) {
        }
        if ((media == null) ||
             !(service.isAttributeValueSupported(media, null, null))) {
            media = (Media)service.getDefaultAttributeValue(Media.class);
        }
        OrientationRequested orient;
        switch (page.getOrientation()) {
        case PageFormat.LANDSCAPE :
            orient = OrientationRequested.LANDSCAPE;
            break;
        case PageFormat.REVERSE_LANDSCAPE:
            orient = OrientationRequested.REVERSE_LANDSCAPE;
            break;
        default:
            orient = OrientationRequested.PORTRAIT;
        }
        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        if (media != null) {
            attributes.add(media);
        }
        attributes.add(orient);
        float ix = (float)(page.getPaper().getImageableX()/DPI);
        float iw = (float)(page.getPaper().getImageableWidth()/DPI);
        float iy = (float)(page.getPaper().getImageableY()/DPI);
        float ih = (float)(page.getPaper().getImageableHeight()/DPI);
        if (ix < 0) ix = 0f; if (iy < 0) iy = 0f;
        try {
            attributes.add(new MediaPrintableArea(ix, iy, iw, ih,
                                                  MediaPrintableArea.INCH));
        } catch (IllegalArgumentException iae) {
        }
    }
    public PageFormat pageDialog(PageFormat page)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        final GraphicsConfiguration gc =
          GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice().getDefaultConfiguration();
        PrintService service =
            (PrintService)java.security.AccessController.doPrivileged(
                                        new java.security.PrivilegedAction() {
                public Object run() {
                    PrintService service = getPrintService();
                    if (service == null) {
                        ServiceDialog.showNoPrintService(gc);
                        return null;
                    }
                    return service;
                }
            });
        if (service == null) {
            return page;
        }
        updatePageAttributes(service, page);
        PageFormat newPage = pageDialog(attributes);
        if (newPage == null) {
            return page;
        } else {
            return newPage;
        }
    }
    public PageFormat pageDialog(final PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        final GraphicsConfiguration gc =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle bounds = gc.getBounds();
        int x = bounds.x+bounds.width/3;
        int y = bounds.y+bounds.height/3;
        PrintService service =
            (PrintService)java.security.AccessController.doPrivileged(
                                        new java.security.PrivilegedAction() {
                public Object run() {
                    PrintService service = getPrintService();
                    if (service == null) {
                        ServiceDialog.showNoPrintService(gc);
                        return null;
                    }
                    return service;
                }
            });
        if (service == null) {
            return null;
        }
        ServiceDialog pageDialog = new ServiceDialog(gc, x, y, service,
                                       DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                       attributes, (Frame)null);
        pageDialog.show();
        if (pageDialog.getStatus() == ServiceDialog.APPROVE) {
            PrintRequestAttributeSet newas =
                pageDialog.getAttributes();
            Class amCategory = SunAlternateMedia.class;
            if (attributes.containsKey(amCategory) &&
                !newas.containsKey(amCategory)) {
                attributes.remove(amCategory);
            }
            attributes.addAll(newas);
            PageFormat page = defaultPage();
            OrientationRequested orient =
                (OrientationRequested)
                attributes.get(OrientationRequested.class);
            int pfOrient =  PageFormat.PORTRAIT;
            if (orient != null) {
                if (orient == OrientationRequested.REVERSE_LANDSCAPE) {
                    pfOrient = PageFormat.REVERSE_LANDSCAPE;
                } else if (orient == OrientationRequested.LANDSCAPE) {
                    pfOrient = PageFormat.LANDSCAPE;
                }
            }
            page.setOrientation(pfOrient);
            Media media = (Media)attributes.get(Media.class);
            if (media == null) {
                media =
                    (Media)service.getDefaultAttributeValue(Media.class);
            }
            if (!(media instanceof MediaSizeName)) {
                media = MediaSizeName.NA_LETTER;
            }
            MediaSize size =
                MediaSize.getMediaSizeForName((MediaSizeName)media);
            if (size == null) {
                size = MediaSize.NA.LETTER;
            }
            Paper paper = new Paper();
            float dim[] = size.getSize(1); 
            double w = Math.rint((dim[0]*72.0)/Size2DSyntax.INCH);
            double h = Math.rint((dim[1]*72.0)/Size2DSyntax.INCH);
            paper.setSize(w, h);
            MediaPrintableArea area =
                (MediaPrintableArea)
                attributes.get(MediaPrintableArea.class);
            double ix, iw, iy, ih;
            if (area != null) {
                ix = Math.rint(
                               area.getX(MediaPrintableArea.INCH) * DPI);
                iy = Math.rint(
                               area.getY(MediaPrintableArea.INCH) * DPI);
                iw = Math.rint(
                               area.getWidth(MediaPrintableArea.INCH) * DPI);
                ih = Math.rint(
                               area.getHeight(MediaPrintableArea.INCH) * DPI);
            }
            else {
                if (w >= 72.0 * 6.0) {
                    ix = 72.0;
                    iw = w - 2 * 72.0;
                } else {
                    ix = w / 6.0;
                    iw = w * 0.75;
                }
                if (h >= 72.0 * 6.0) {
                    iy = 72.0;
                    ih = h - 2 * 72.0;
                } else {
                    iy = h / 6.0;
                    ih = h * 0.75;
                }
            }
            paper.setImageableArea(ix, iy, iw, ih);
            page.setPaper(paper);
            return page;
        } else {
            return null;
        }
   }
    public boolean printDialog(final PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        DialogTypeSelection dlg =
            (DialogTypeSelection)attributes.get(DialogTypeSelection.class);
        if (dlg == DialogTypeSelection.NATIVE) {
            this.attributes = attributes;
            try {
                debug_println("calling setAttributes in printDialog");
                setAttributes(attributes);
            } catch (PrinterException e) {
            }
            boolean ret = printDialog();
            this.attributes = attributes;
            return ret;
        }
        final GraphicsConfiguration gc =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
        PrintService service =
            (PrintService)java.security.AccessController.doPrivileged(
                       new java.security.PrivilegedAction() {
                public Object run() {
                    PrintService service = getPrintService();
                    if (service == null) {
                        ServiceDialog.showNoPrintService(gc);
                        return null;
                    }
                    return service;
                }
            });
        if (service == null) {
            return false;
        }
        PrintService[] services;
        StreamPrintServiceFactory[] spsFactories = null;
        if (service instanceof StreamPrintService) {
            spsFactories = lookupStreamPrintServices(null);
            services = new StreamPrintService[spsFactories.length];
            for (int i=0; i<spsFactories.length; i++) {
                services[i] = spsFactories[i].getPrintService(null);
            }
        } else {
            services =
            (PrintService[])java.security.AccessController.doPrivileged(
                       new java.security.PrivilegedAction() {
                public Object run() {
                    PrintService[] services = PrinterJob.lookupPrintServices();
                    return services;
                }
            });
            if ((services == null) || (services.length == 0)) {
                services = new PrintService[1];
                services[0] = service;
            }
        }
        Rectangle bounds = gc.getBounds();
        int x = bounds.x+bounds.width/3;
        int y = bounds.y+bounds.height/3;
        PrintService newService;
        try {
            newService =
            ServiceUI.printDialog(gc, x, y,
                                  services, service,
                                  DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                  attributes);
        } catch (IllegalArgumentException iae) {
            newService = ServiceUI.printDialog(gc, x, y,
                                  services, services[0],
                                  DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                  attributes);
        }
        if (newService == null) {
            return false;
        }
        if (!service.equals(newService)) {
            try {
                setPrintService(newService);
            } catch (PrinterException e) {
                myService = newService;
            }
        }
        return true;
    }
    public boolean printDialog() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        PrintRequestAttributeSet attributes =
          new HashPrintRequestAttributeSet();
        attributes.add(new Copies(getCopies()));
        attributes.add(new JobName(getJobName(), null));
        boolean doPrint = printDialog(attributes);
        if (doPrint) {
            JobName jobName = (JobName)attributes.get(JobName.class);
            if (jobName != null) {
                setJobName(jobName.getValue());
            }
            Copies copies = (Copies)attributes.get(Copies.class);
            if (copies != null) {
                setCopies(copies.getValue());
            }
            Destination dest = (Destination)attributes.get(Destination.class);
            if (dest != null) {
                try {
                    mDestType = RasterPrinterJob.FILE;
                    mDestination = (new File(dest.getURI())).getPath();
                } catch (Exception e) {
                    mDestination = "out.prn";
                    PrintService ps = getPrintService();
                    if (ps != null) {
                        Destination defaultDest = (Destination)ps.
                            getDefaultAttributeValue(Destination.class);
                        if (defaultDest != null) {
                            mDestination = (new File(defaultDest.getURI())).getPath();
                        }
                    }
                }
            } else {
                mDestType = RasterPrinterJob.PRINTER;
                PrintService ps = getPrintService();
                if (ps != null) {
                    mDestination = ps.getName();
                }
            }
        }
        return doPrint;
    }
    public void setPrintable(Printable painter) {
        setPageable(new OpenBook(defaultPage(new PageFormat()), painter));
    }
    public void setPrintable(Printable painter, PageFormat format) {
        setPageable(new OpenBook(format, painter));
        updatePageAttributes(getPrintService(), format);
    }
    public void setPageable(Pageable document) throws NullPointerException {
        if (document != null) {
            mDocument = document;
        } else {
            throw new NullPointerException();
        }
    }
    protected void initPrinter() {
        return;
    }
    protected boolean isSupportedValue(Attribute attrval,
                                     PrintRequestAttributeSet attrset) {
        PrintService ps = getPrintService();
        return
            (attrval != null && ps != null &&
             ps.isAttributeValueSupported(attrval,
                                          DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                          attrset));
    }
    protected  void setAttributes(PrintRequestAttributeSet attributes)
        throws PrinterException {
        setCollated(false);
        sidesAttr = null;
        pageRangesAttr = null;
        copiesAttr = 0;
        jobNameAttr = null;
        userNameAttr = null;
        destinationAttr = null;
        collateAttReq = false;
        PrintService service = getPrintService();
        if (attributes == null  || service == null) {
            return;
        }
        boolean fidelity = false;
        Fidelity attrFidelity = (Fidelity)attributes.get(Fidelity.class);
        if (attrFidelity != null && attrFidelity == Fidelity.FIDELITY_TRUE) {
            fidelity = true;
        }
        if (fidelity == true) {
           AttributeSet unsupported =
               service.getUnsupportedAttributes(
                                         DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                         attributes);
           if (unsupported != null) {
               throw new PrinterException("Fidelity cannot be satisfied");
           }
        }
        SheetCollate collateAttr =
            (SheetCollate)attributes.get(SheetCollate.class);
        if (isSupportedValue(collateAttr,  attributes)) {
            setCollated(collateAttr == SheetCollate.COLLATED);
        }
        sidesAttr = (Sides)attributes.get(Sides.class);
        if (!isSupportedValue(sidesAttr,  attributes)) {
            sidesAttr = Sides.ONE_SIDED;
        }
        pageRangesAttr =  (PageRanges)attributes.get(PageRanges.class);
        if (!isSupportedValue(pageRangesAttr, attributes)) {
            pageRangesAttr = null;
        } else {
            if ((SunPageSelection)attributes.get(SunPageSelection.class)
                     == SunPageSelection.RANGE) {
                int[][] range = pageRangesAttr.getMembers();
                setPageRange(range[0][0] - 1, range[0][1] - 1);
            } else {
               setPageRange(-1, - 1);
            }
        }
        Copies copies = (Copies)attributes.get(Copies.class);
        if (isSupportedValue(copies,  attributes) ||
            (!fidelity && copies != null)) {
            copiesAttr = copies.getValue();
            setCopies(copiesAttr);
        } else {
            copiesAttr = getCopies();
        }
        Destination destination =
            (Destination)attributes.get(Destination.class);
        if (isSupportedValue(destination,  attributes)) {
            try {
                destinationAttr = "" + new File(destination.getURI().
                                                getSchemeSpecificPart());
            } catch (Exception e) { 
                Destination defaultDest = (Destination)service.
                    getDefaultAttributeValue(Destination.class);
                if (defaultDest != null) {
                    destinationAttr = "" + new File(defaultDest.getURI().
                                                getSchemeSpecificPart());
                }
            }
        }
        JobSheets jobSheets = (JobSheets)attributes.get(JobSheets.class);
        if (jobSheets != null) {
            noJobSheet = jobSheets == JobSheets.NONE;
        }
        JobName jobName = (JobName)attributes.get(JobName.class);
        if (isSupportedValue(jobName,  attributes) ||
            (!fidelity && jobName != null)) {
            jobNameAttr = jobName.getValue();
            setJobName(jobNameAttr);
        } else {
            jobNameAttr = getJobName();
        }
        RequestingUserName userName =
            (RequestingUserName)attributes.get(RequestingUserName.class);
        if (isSupportedValue(userName,  attributes) ||
            (!fidelity && userName != null)) {
            userNameAttr = userName.getValue();
        } else {
            try {
                userNameAttr = getUserName();
            } catch (SecurityException e) {
                userNameAttr = "";
            }
        }
        Media media = (Media)attributes.get(Media.class);
        OrientationRequested orientReq =
           (OrientationRequested)attributes.get(OrientationRequested.class);
        MediaPrintableArea mpa =
            (MediaPrintableArea)attributes.get(MediaPrintableArea.class);
        if ((orientReq != null || media != null || mpa != null) &&
            getPageable() instanceof OpenBook) {
            Pageable pageable = getPageable();
            Printable printable = pageable.getPrintable(0);
            PageFormat pf = (PageFormat)pageable.getPageFormat(0).clone();
            Paper paper = pf.getPaper();
            if (mpa == null && media != null &&
                service.
                isAttributeCategorySupported(MediaPrintableArea.class)) {
                Object mpaVals = service.
                    getSupportedAttributeValues(MediaPrintableArea.class,
                                                null, attributes);
                if (mpaVals instanceof MediaPrintableArea[] &&
                    ((MediaPrintableArea[])mpaVals).length > 0) {
                    mpa = ((MediaPrintableArea[])mpaVals)[0];
                }
            }
            if (isSupportedValue(orientReq, attributes) ||
                (!fidelity && orientReq != null)) {
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
            if (isSupportedValue(media, attributes) ||
                (!fidelity && media != null)) {
                if (media instanceof MediaSizeName) {
                    MediaSizeName msn = (MediaSizeName)media;
                    MediaSize msz = MediaSize.getMediaSizeForName(msn);
                    if (msz != null) {
                        float paperWid =  msz.getX(MediaSize.INCH) * 72.0f;
                        float paperHgt =  msz.getY(MediaSize.INCH) * 72.0f;
                        paper.setSize(paperWid, paperHgt);
                        if (mpa == null) {
                            paper.setImageableArea(72.0, 72.0,
                                                   paperWid-144.0,
                                                   paperHgt-144.0);
                        }
                    }
                }
            }
            if (isSupportedValue(mpa, attributes) ||
                (!fidelity && mpa != null)) {
                float [] printableArea =
                    mpa.getPrintableArea(MediaPrintableArea.INCH);
                for (int i=0; i < printableArea.length; i++) {
                    printableArea[i] = printableArea[i]*72.0f;
                }
                paper.setImageableArea(printableArea[0], printableArea[1],
                                       printableArea[2], printableArea[3]);
            }
            pf.setPaper(paper);
            pf = validatePage(pf);
            setPrintable(printable, pf);
        } else {
            this.attributes = attributes;
        }
    }
    private void spoolToService(PrintService psvc,
                                PrintRequestAttributeSet attributes)
        throws PrinterException {
        if (psvc == null) {
            throw new PrinterException("No print service found.");
        }
        DocPrintJob job = psvc.createPrintJob();
        Doc doc = new PageableDoc(getPageable());
        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        try {
            job.print(doc, attributes);
        } catch (PrintException e) {
            throw new PrinterException(e.toString());
        }
    }
    public void print() throws PrinterException {
        print(attributes);
    }
    public static boolean debugPrint = false;
    protected void debug_println(String str) {
        if (debugPrint) {
            System.out.println("RasterPrinterJob "+str+" "+this);
        }
    }
    public void print(PrintRequestAttributeSet attributes)
        throws PrinterException {
        PrintService psvc = getPrintService();
        debug_println("psvc = "+psvc);
        if (psvc == null) {
            throw new PrinterException("No print service found.");
        }
        PrinterState prnState = (PrinterState)psvc.getAttribute(
                                                  PrinterState.class);
        if (prnState == PrinterState.STOPPED) {
            PrinterStateReasons prnStateReasons =
                    (PrinterStateReasons)psvc.getAttribute(
                                                 PrinterStateReasons.class);
                if ((prnStateReasons != null) &&
                    (prnStateReasons.containsKey(PrinterStateReason.SHUTDOWN)))
                {
                    throw new PrinterException("PrintService is no longer available.");
                }
        }
        if ((PrinterIsAcceptingJobs)(psvc.getAttribute(
                         PrinterIsAcceptingJobs.class)) ==
                         PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS) {
            throw new PrinterException("Printer is not accepting job.");
        }
        if ((psvc instanceof SunPrinterJobService) &&
            ((SunPrinterJobService)psvc).usesClass(getClass())) {
            setAttributes(attributes);
            if (destinationAttr != null) {
                File f = new File(destinationAttr);
                try {
                    if (f.createNewFile()) {
                        f.delete();
                    }
                } catch (IOException ioe) {
                    throw new PrinterException("Cannot write to file:"+
                                               destinationAttr);
                } catch (SecurityException se) {
                }
                File pFile = f.getParentFile();
                if ((f.exists() &&
                     (!f.isFile() || !f.canWrite())) ||
                    ((pFile != null) &&
                     (!pFile.exists() || (pFile.exists() && !pFile.canWrite())))) {
                    throw new PrinterException("Cannot write to file:"+
                                               destinationAttr);
                }
            }
        } else {
            spoolToService(psvc, attributes);
            return;
        }
        initPrinter();
        int numCollatedCopies = getCollatedCopies();
        int numNonCollatedCopies = getNoncollatedCopies();
        debug_println("getCollatedCopies()  "+numCollatedCopies
              + " getNoncollatedCopies() "+ numNonCollatedCopies);
        int numPages = mDocument.getNumberOfPages();
        if (numPages == 0) {
            return;
        }
        int firstPage = getFirstPage();
        int lastPage = getLastPage();
        if(lastPage == Pageable.UNKNOWN_NUMBER_OF_PAGES){
            int totalPages = mDocument.getNumberOfPages();
            if (totalPages != Pageable.UNKNOWN_NUMBER_OF_PAGES) {
                lastPage = mDocument.getNumberOfPages() - 1;
            }
        }
        try {
            synchronized (this) {
                performingPrinting = true;
                userCancelled = false;
            }
            startDoc();
            if (isCancelled()) {
                cancelDoc();
            }
            boolean rangeIsSelected = true;
            if (attributes != null) {
                SunPageSelection pages =
                    (SunPageSelection)attributes.get(SunPageSelection.class);
                if ((pages != null) && (pages != SunPageSelection.RANGE)) {
                    rangeIsSelected = false;
                }
            }
            debug_println("after startDoc rangeSelected? "+rangeIsSelected
                      + " numNonCollatedCopies "+ numNonCollatedCopies);
            for(int collated = 0; collated < numCollatedCopies; collated++) {
                for(int i = firstPage, pageResult = Printable.PAGE_EXISTS;
                    (i <= lastPage ||
                     lastPage == Pageable.UNKNOWN_NUMBER_OF_PAGES)
                    && pageResult == Printable.PAGE_EXISTS;
                    i++)
                {
                    if ((pageRangesAttr != null) && rangeIsSelected ){
                        int nexti = pageRangesAttr.next(i);
                        if (nexti == -1) {
                            break;
                        } else if (nexti != i+1) {
                            continue;
                        }
                    }
                    for(int nonCollated = 0;
                        nonCollated < numNonCollatedCopies
                        && pageResult == Printable.PAGE_EXISTS;
                        nonCollated++)
                    {
                        if (isCancelled()) {
                            cancelDoc();
                        }
                        debug_println("printPage "+i);
                        pageResult = printPage(mDocument, i);
                    }
                }
            }
            if (isCancelled()) {
                cancelDoc();
            }
        } finally {
            previousPaper = null;
            synchronized (this) {
                if (performingPrinting) {
                    endDoc();
                }
                performingPrinting = false;
                notify();
            }
        }
    }
    protected void validatePaper(Paper origPaper, Paper newPaper) {
        if (origPaper == null || newPaper == null) {
            return;
        } else {
            double wid = origPaper.getWidth();
            double hgt = origPaper.getHeight();
            double ix = origPaper.getImageableX();
            double iy = origPaper.getImageableY();
            double iw = origPaper.getImageableWidth();
            double ih = origPaper.getImageableHeight();
            Paper defaultPaper = new Paper();
            wid = ((wid > 0.0) ? wid : defaultPaper.getWidth());
            hgt = ((hgt > 0.0) ? hgt : defaultPaper.getHeight());
            ix = ((ix > 0.0) ? ix : defaultPaper.getImageableX());
            iy = ((iy > 0.0) ? iy : defaultPaper.getImageableY());
            iw = ((iw > 0.0) ? iw : defaultPaper.getImageableWidth());
            ih = ((ih > 0.0) ? ih : defaultPaper.getImageableHeight());
            if (iw > wid) {
                iw = wid;
            }
            if (ih > hgt) {
                ih = hgt;
            }
            if ((ix + iw) > wid) {
                ix = wid - iw;
            }
            if ((iy + ih) > hgt) {
                iy = hgt - ih;
            }
            newPaper.setSize(wid, hgt);
            newPaper.setImageableArea(ix, iy, iw, ih);
        }
    }
    public PageFormat defaultPage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        newPage.setOrientation(PageFormat.PORTRAIT);
        Paper newPaper = new Paper();
        double ptsPerInch = 72.0;
        double w, h;
        Media media = null;
        PrintService service = getPrintService();
        if (service != null) {
            MediaSize size;
            media =
                (Media)service.getDefaultAttributeValue(Media.class);
            if (media instanceof MediaSizeName &&
               ((size = MediaSize.getMediaSizeForName((MediaSizeName)media)) !=
                null)) {
                w =  size.getX(MediaSize.INCH) * ptsPerInch;
                h =  size.getY(MediaSize.INCH) * ptsPerInch;
                newPaper.setSize(w, h);
                newPaper.setImageableArea(ptsPerInch, ptsPerInch,
                                          w - 2.0*ptsPerInch,
                                          h - 2.0*ptsPerInch);
                newPage.setPaper(newPaper);
                return newPage;
            }
        }
        String defaultCountry = Locale.getDefault().getCountry();
        if (!Locale.getDefault().equals(Locale.ENGLISH) && 
            defaultCountry != null &&
            !defaultCountry.equals(Locale.US.getCountry()) &&
            !defaultCountry.equals(Locale.CANADA.getCountry())) {
            double mmPerInch = 25.4;
            w = Math.rint((210.0*ptsPerInch)/mmPerInch);
            h = Math.rint((297.0*ptsPerInch)/mmPerInch);
            newPaper.setSize(w, h);
            newPaper.setImageableArea(ptsPerInch, ptsPerInch,
                                      w - 2.0*ptsPerInch,
                                      h - 2.0*ptsPerInch);
        }
        newPage.setPaper(newPaper);
        return newPage;
    }
    public PageFormat validatePage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        Paper newPaper = new Paper();
        validatePaper(newPage.getPaper(), newPaper);
        newPage.setPaper(newPaper);
        return newPage;
    }
    public void setCopies(int copies) {
        mNumCopies = copies;
    }
    public int getCopies() {
        return mNumCopies;
    }
    protected int getCopiesInt() {
        return (copiesAttr > 0) ? copiesAttr : getCopies();
    }
    public String getUserName() {
        return System.getProperty("user.name");
    }
    protected String getUserNameInt() {
        if  (userNameAttr != null) {
            return userNameAttr;
        } else {
            try {
                return  getUserName();
            } catch (SecurityException e) {
                return "";
            }
        }
    }
    public void setJobName(String jobName) {
        if (jobName != null) {
            mDocName = jobName;
        } else {
            throw new NullPointerException();
        }
    }
    public String getJobName() {
        return mDocName;
    }
    protected String getJobNameInt() {
        return (jobNameAttr != null) ? jobNameAttr : getJobName();
    }
    protected void setPageRange(int firstPage, int lastPage) {
        if(firstPage >= 0 && lastPage >= 0) {
            mFirstPage = firstPage;
            mLastPage = lastPage;
            if(mLastPage < mFirstPage) mLastPage = mFirstPage;
        } else {
            mFirstPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
            mLastPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
        }
    }
    protected int getFirstPage() {
        return mFirstPage == Book.UNKNOWN_NUMBER_OF_PAGES ? 0 : mFirstPage;
    }
    protected int getLastPage() {
        return mLastPage;
    }
    protected void setCollated(boolean collate) {
        mCollate = collate;
        collateAttReq = true;
    }
    protected boolean isCollated() {
            return mCollate;
    }
    protected abstract void startDoc() throws PrinterException;
    protected abstract void endDoc() throws PrinterException;
    protected abstract void abortDoc();
    private void cancelDoc() throws PrinterAbortException {
        abortDoc();
        synchronized (this) {
            userCancelled = false;
            performingPrinting = false;
            notify();
        }
        throw new PrinterAbortException();
    }
    protected int getCollatedCopies() {
        return isCollated() ? getCopiesInt() : 1;
    }
    protected int getNoncollatedCopies() {
        return isCollated() ? 1 : getCopiesInt();
    }
    private int deviceWidth, deviceHeight;
    private AffineTransform defaultDeviceTransform;
    private PrinterGraphicsConfig pgConfig;
    synchronized void setGraphicsConfigInfo(AffineTransform at,
                                            double pw, double ph) {
        Point2D.Double pt = new Point2D.Double(pw, ph);
        at.transform(pt, pt);
        if (pgConfig == null ||
            defaultDeviceTransform == null ||
            !at.equals(defaultDeviceTransform) ||
            deviceWidth != (int)pt.getX() ||
            deviceHeight != (int)pt.getY()) {
                deviceWidth = (int)pt.getX();
                deviceHeight = (int)pt.getY();
                defaultDeviceTransform = at;
                pgConfig = null;
        }
    }
    synchronized PrinterGraphicsConfig getPrinterGraphicsConfig() {
        if (pgConfig != null) {
            return pgConfig;
        }
        String deviceID = "Printer Device";
        PrintService service = getPrintService();
        if (service != null) {
            deviceID = service.toString();
        }
        pgConfig = new PrinterGraphicsConfig(deviceID,
                                             defaultDeviceTransform,
                                             deviceWidth, deviceHeight);
        return pgConfig;
    }
    protected int printPage(Pageable document, int pageIndex)
        throws PrinterException
    {
        PageFormat page;
        PageFormat origPage;
        Printable painter;
        try {
            origPage = document.getPageFormat(pageIndex);
            page = (PageFormat)origPage.clone();
            painter = document.getPrintable(pageIndex);
        } catch (Exception e) {
            PrinterException pe =
                    new PrinterException("Error getting page or printable.[ " +
                                          e +" ]");
            pe.initCause(e);
            throw pe;
        }
        Paper paper = page.getPaper();
        if (page.getOrientation() != PageFormat.PORTRAIT &&
            landscapeRotates270) {
            double left = paper.getImageableX();
            double top = paper.getImageableY();
            double width = paper.getImageableWidth();
            double height = paper.getImageableHeight();
            paper.setImageableArea(paper.getWidth()-left-width,
                                   paper.getHeight()-top-height,
                                   width, height);
            page.setPaper(paper);
            if (page.getOrientation() == PageFormat.LANDSCAPE) {
                page.setOrientation(PageFormat.REVERSE_LANDSCAPE);
            } else {
                page.setOrientation(PageFormat.LANDSCAPE);
            }
        }
        double xScale = getXRes() / 72.0;
        double yScale = getYRes() / 72.0;
        Rectangle2D deviceArea =
            new Rectangle2D.Double(paper.getImageableX() * xScale,
                                   paper.getImageableY() * yScale,
                                   paper.getImageableWidth() * xScale,
                                   paper.getImageableHeight() * yScale);
        AffineTransform uniformTransform = new AffineTransform();
        AffineTransform scaleTransform = new AffineTransform();
        scaleTransform.scale(xScale, yScale);
        int bandWidth = (int) deviceArea.getWidth();
        if (bandWidth % 4 != 0) {
            bandWidth += (4 - (bandWidth % 4));
        }
        if (bandWidth <= 0) {
            throw new PrinterException("Paper's imageable width is too small.");
        }
        int deviceAreaHeight = (int)deviceArea.getHeight();
        if (deviceAreaHeight <= 0) {
            throw new PrinterException("Paper's imageable height is too small.");
        }
        int bandHeight = (int)(MAX_BAND_SIZE / bandWidth / 3);
        int deviceLeft = (int)Math.rint(paper.getImageableX() * xScale);
        int deviceTop  = (int)Math.rint(paper.getImageableY() * yScale);
        AffineTransform deviceTransform = new AffineTransform();
        deviceTransform.translate(-deviceLeft, deviceTop);
        deviceTransform.translate(0, bandHeight);
        deviceTransform.scale(1, -1);
        BufferedImage pBand = new BufferedImage(1, 1,
                                                BufferedImage.TYPE_3BYTE_BGR);
        PeekGraphics peekGraphics = createPeekGraphics(pBand.createGraphics(),
                                                       this);
        Rectangle2D.Double pageFormatArea =
            new Rectangle2D.Double(page.getImageableX(),
                                   page.getImageableY(),
                                   page.getImageableWidth(),
                                   page.getImageableHeight());
        peekGraphics.transform(scaleTransform);
        peekGraphics.translate(-getPhysicalPrintableX(paper) / xScale,
                               -getPhysicalPrintableY(paper) / yScale);
        peekGraphics.transform(new AffineTransform(page.getMatrix()));
        initPrinterGraphics(peekGraphics, pageFormatArea);
        AffineTransform pgAt = peekGraphics.getTransform();
        setGraphicsConfigInfo(scaleTransform,
                              paper.getWidth(), paper.getHeight());
        int pageResult = painter.print(peekGraphics, origPage, pageIndex);
        debug_println("pageResult "+pageResult);
        if (pageResult == Printable.PAGE_EXISTS) {
            debug_println("startPage "+pageIndex);
            Paper thisPaper = page.getPaper();
            boolean paperChanged =
                previousPaper == null ||
                thisPaper.getWidth() != previousPaper.getWidth() ||
                thisPaper.getHeight() != previousPaper.getHeight();
            previousPaper = thisPaper;
            startPage(page, painter, pageIndex, paperChanged);
            Graphics2D pathGraphics = createPathGraphics(peekGraphics, this,
                                                         painter, page,
                                                         pageIndex);
            if (pathGraphics != null) {
                pathGraphics.transform(scaleTransform);
                pathGraphics.translate(-getPhysicalPrintableX(paper) / xScale,
                                       -getPhysicalPrintableY(paper) / yScale);
                pathGraphics.transform(new AffineTransform(page.getMatrix()));
                initPrinterGraphics(pathGraphics, pageFormatArea);
                redrawList.clear();
                AffineTransform initialTx = pathGraphics.getTransform();
                painter.print(pathGraphics, origPage, pageIndex);
                for (int i=0;i<redrawList.size();i++) {
                   GraphicsState gstate = (GraphicsState)redrawList.get(i);
                   pathGraphics.setTransform(initialTx);
                   ((PathGraphics)pathGraphics).redrawRegion(
                                                         gstate.region,
                                                         gstate.sx,
                                                         gstate.sy,
                                                         gstate.theClip,
                                                         gstate.theTransform);
                }
            } else {
                BufferedImage band = cachedBand;
                if (cachedBand == null ||
                    bandWidth != cachedBandWidth ||
                    bandHeight != cachedBandHeight) {
                    band = new BufferedImage(bandWidth, bandHeight,
                                             BufferedImage.TYPE_3BYTE_BGR);
                    cachedBand = band;
                    cachedBandWidth = bandWidth;
                    cachedBandHeight = bandHeight;
                }
                Graphics2D bandGraphics = band.createGraphics();
                Rectangle2D.Double clipArea =
                    new Rectangle2D.Double(0, 0, bandWidth, bandHeight);
                initPrinterGraphics(bandGraphics, clipArea);
                ProxyGraphics2D painterGraphics =
                    new ProxyGraphics2D(bandGraphics, this);
                Graphics2D clearGraphics = band.createGraphics();
                clearGraphics.setColor(Color.white);
                ByteInterleavedRaster tile = (ByteInterleavedRaster)band.getRaster();
                byte[] data = tile.getDataStorage();
                int deviceBottom = deviceTop + deviceAreaHeight;
                int deviceAddressableX = (int)getPhysicalPrintableX(paper);
                int deviceAddressableY = (int)getPhysicalPrintableY(paper);
                for (int bandTop = 0; bandTop <= deviceAreaHeight;
                     bandTop += bandHeight)
                {
                    clearGraphics.fillRect(0, 0, bandWidth, bandHeight);
                    bandGraphics.setTransform(uniformTransform);
                    bandGraphics.transform(deviceTransform);
                    deviceTransform.translate(0, -bandHeight);
                    bandGraphics.transform(scaleTransform);
                    bandGraphics.transform(new AffineTransform(page.getMatrix()));
                    Rectangle clip = bandGraphics.getClipBounds();
                    clip = pgAt.createTransformedShape(clip).getBounds();
                    if ((clip == null) || peekGraphics.hitsDrawingArea(clip) &&
                        (bandWidth > 0 && bandHeight > 0)) {
                        int bandX = deviceLeft - deviceAddressableX;
                        if (bandX < 0) {
                            bandGraphics.translate(bandX/xScale,0);
                            bandX = 0;
                        }
                        int bandY = deviceTop + bandTop - deviceAddressableY;
                        if (bandY < 0) {
                            bandGraphics.translate(0,bandY/yScale);
                            bandY = 0;
                        }
                        painterGraphics.setDelegate((Graphics2D) bandGraphics.create());
                        painter.print(painterGraphics, origPage, pageIndex);
                        painterGraphics.dispose();
                        printBand(data, bandX, bandY, bandWidth, bandHeight);
                    }
                }
                clearGraphics.dispose();
                bandGraphics.dispose();
            }
            debug_println("calling endPage "+pageIndex);
            endPage(page, painter, pageIndex);
        }
        return pageResult;
    }
    public void cancel() {
        synchronized (this) {
            if (performingPrinting) {
                userCancelled = true;
            }
            notify();
        }
    }
    public boolean isCancelled() {
        boolean cancelled = false;
        synchronized (this) {
            cancelled = (performingPrinting && userCancelled);
            notify();
        }
        return cancelled;
    }
    protected Pageable getPageable() {
        return mDocument;
    }
    protected Graphics2D createPathGraphics(PeekGraphics graphics,
                                            PrinterJob printerJob,
                                            Printable painter,
                                            PageFormat pageFormat,
                                            int pageIndex) {
        return null;
    }
    protected PeekGraphics createPeekGraphics(Graphics2D graphics,
                                              PrinterJob printerJob) {
        return new PeekGraphics(graphics, printerJob);
    }
    void initPrinterGraphics(Graphics2D g, Rectangle2D clip) {
        g.setClip(clip);
        g.setPaint(Color.black);
    }
    public boolean checkAllowedToPrintToFile() {
        try {
            throwPrintToFile();
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
    private void throwPrintToFile() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (printToFilePermission == null) {
                printToFilePermission =
                    new FilePermission("<<ALL FILES>>", "read,write");
            }
            security.checkPermission(printToFilePermission);
        }
    }
    protected String removeControlChars(String s) {
        char[] in_chars = s.toCharArray();
        int len = in_chars.length;
        char[] out_chars = new char[len];
        int pos = 0;
        for (int i = 0; i < len; i++) {
            char c = in_chars[i];
            if (c > '\r' || c < '\t' || c == '\u000b' || c == '\u000c')  {
               out_chars[pos++] = c;
            }
        }
        if (pos == len) {
            return s; 
        } else {
            return new String(out_chars, 0, pos);
        }
    }
}
