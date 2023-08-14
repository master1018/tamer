public class PrintJob2D extends PrintJob implements Printable, Runnable {
    private static final MediaType SIZES[] = {
        MediaType.ISO_4A0, MediaType.ISO_2A0, MediaType.ISO_A0,
        MediaType.ISO_A1, MediaType.ISO_A2, MediaType.ISO_A3,
        MediaType.ISO_A4, MediaType.ISO_A5, MediaType.ISO_A6,
        MediaType.ISO_A7, MediaType.ISO_A8, MediaType.ISO_A9,
        MediaType.ISO_A10, MediaType.ISO_B0, MediaType.ISO_B1,
        MediaType.ISO_B2, MediaType.ISO_B3, MediaType.ISO_B4,
        MediaType.ISO_B5, MediaType.ISO_B6, MediaType.ISO_B7,
        MediaType.ISO_B8, MediaType.ISO_B9, MediaType.ISO_B10,
        MediaType.JIS_B0, MediaType.JIS_B1, MediaType.JIS_B2,
        MediaType.JIS_B3, MediaType.JIS_B4, MediaType.JIS_B5,
        MediaType.JIS_B6, MediaType.JIS_B7, MediaType.JIS_B8,
        MediaType.JIS_B9, MediaType.JIS_B10, MediaType.ISO_C0,
        MediaType.ISO_C1, MediaType.ISO_C2, MediaType.ISO_C3,
        MediaType.ISO_C4, MediaType.ISO_C5, MediaType.ISO_C6,
        MediaType.ISO_C7, MediaType.ISO_C8, MediaType.ISO_C9,
        MediaType.ISO_C10, MediaType.ISO_DESIGNATED_LONG,
        MediaType.EXECUTIVE, MediaType.FOLIO, MediaType.INVOICE,
        MediaType.LEDGER, MediaType.NA_LETTER, MediaType.NA_LEGAL,
        MediaType.QUARTO, MediaType.A, MediaType.B,
        MediaType.C, MediaType.D, MediaType.E,
        MediaType.NA_10X15_ENVELOPE, MediaType.NA_10X14_ENVELOPE,
        MediaType.NA_10X13_ENVELOPE, MediaType.NA_9X12_ENVELOPE,
        MediaType.NA_9X11_ENVELOPE, MediaType.NA_7X9_ENVELOPE,
        MediaType.NA_6X9_ENVELOPE, MediaType.NA_NUMBER_9_ENVELOPE,
        MediaType.NA_NUMBER_10_ENVELOPE, MediaType.NA_NUMBER_11_ENVELOPE,
        MediaType.NA_NUMBER_12_ENVELOPE, MediaType.NA_NUMBER_14_ENVELOPE,
        MediaType.INVITE_ENVELOPE, MediaType.ITALY_ENVELOPE,
        MediaType.MONARCH_ENVELOPE, MediaType.PERSONAL_ENVELOPE
    };
    private static final MediaSizeName JAVAXSIZES[] = {
        null, null, MediaSizeName.ISO_A0,
        MediaSizeName.ISO_A1, MediaSizeName.ISO_A2, MediaSizeName.ISO_A3,
        MediaSizeName.ISO_A4, MediaSizeName.ISO_A5, MediaSizeName.ISO_A6,
        MediaSizeName.ISO_A7, MediaSizeName.ISO_A8, MediaSizeName.ISO_A9,
        MediaSizeName.ISO_A10, MediaSizeName.ISO_B0, MediaSizeName.ISO_B1,
        MediaSizeName.ISO_B2, MediaSizeName.ISO_B3, MediaSizeName.ISO_B4,
        MediaSizeName.ISO_B5,  MediaSizeName.ISO_B6, MediaSizeName.ISO_B7,
        MediaSizeName.ISO_B8, MediaSizeName.ISO_B9, MediaSizeName.ISO_B10,
        MediaSizeName.JIS_B0, MediaSizeName.JIS_B1, MediaSizeName.JIS_B2,
        MediaSizeName.JIS_B3, MediaSizeName.JIS_B4, MediaSizeName.JIS_B5,
        MediaSizeName.JIS_B6, MediaSizeName.JIS_B7, MediaSizeName.JIS_B8,
        MediaSizeName.JIS_B9, MediaSizeName.JIS_B10, MediaSizeName.ISO_C0,
        MediaSizeName.ISO_C1, MediaSizeName.ISO_C2, MediaSizeName.ISO_C3,
        MediaSizeName.ISO_C4, MediaSizeName.ISO_C5, MediaSizeName.ISO_C6,
        null, null, null, null,
        MediaSizeName.ISO_DESIGNATED_LONG, MediaSizeName.EXECUTIVE,
        MediaSizeName.FOLIO, MediaSizeName.INVOICE, MediaSizeName.LEDGER,
        MediaSizeName.NA_LETTER, MediaSizeName.NA_LEGAL,
        MediaSizeName.QUARTO, MediaSizeName.A, MediaSizeName.B,
        MediaSizeName.C, MediaSizeName.D, MediaSizeName.E,
        MediaSizeName.NA_10X15_ENVELOPE, MediaSizeName.NA_10X14_ENVELOPE,
        MediaSizeName.NA_10X13_ENVELOPE, MediaSizeName.NA_9X12_ENVELOPE,
        MediaSizeName.NA_9X11_ENVELOPE, MediaSizeName.NA_7X9_ENVELOPE,
        MediaSizeName.NA_6X9_ENVELOPE,
        MediaSizeName.NA_NUMBER_9_ENVELOPE,
        MediaSizeName.NA_NUMBER_10_ENVELOPE,
        MediaSizeName.NA_NUMBER_11_ENVELOPE,
        MediaSizeName.NA_NUMBER_12_ENVELOPE,
        MediaSizeName.NA_NUMBER_14_ENVELOPE,
        null, MediaSizeName.ITALY_ENVELOPE,
        MediaSizeName.MONARCH_ENVELOPE, MediaSizeName.PERSONAL_ENVELOPE,
    };
    private static final int WIDTHS[] = {
         4768,  3370,  2384,  1684,
         1191,  842,  595,  420,
         298,  210,  147,  105,
         74,  2835,  2004,  1417,
         1001,  709,  499,  354,
         249,  176,  125,  88,
         2920,  2064,  1460,  1032,
         729,  516,  363,  258,
         181,  128,  91,  2599,
         1837,  1298,  918,  649,
         459,  323,  230,  162,
         113,  79,  312,
         522,  612,  396,  792,
         612,  612,  609,  612,
         792,  1224,  1584,  2448,
         720,  720,
         720,  648,
         648,  504,
         432,  279,
         297,  324,
         342,  360,
         624,  312,
         279,  261
    };
    private static final int LENGTHS[] = {
         6741,  4768,  3370,  2384,
         1684,  1191,  842,  595,
         420,  298,  210,  147,
         105,  4008,  2835,  2004,
         1417,  1001,  729,  499,
         354,  249,  176,  125,
         4127,  2920,  2064,  1460,
         1032,  729,  516,  363,
         258,  181,  128,  3677,
         2599,  1837,  1298,  918,
         649,  459,  323,  230,
         162,  113,  624,
         756,  936,  612,  1224,
         792,  1008,  780,  792,
         1224,  1584,  2448,  3168,
         1080,  1008,
         936,  864,
         792,  648,
         648,  639,
         684,  747,
         792,  828,
         624,  652,
         540,  468
    };
    private Frame frame;
    private String docTitle = "";
    private JobAttributes jobAttributes;
    private PageAttributes pageAttributes;
    private PrintRequestAttributeSet attributes;
    private PrinterJob printerJob;
    private PageFormat pageFormat;
    private MessageQ graphicsToBeDrawn = new MessageQ("tobedrawn");
    private MessageQ graphicsDrawn = new MessageQ("drawn");
    private Graphics2D currentGraphics;
    private int pageIndex = -1;
    private final static String DEST_PROP = "awt.print.destination";
    private final static String PRINTER = "printer";
    private final static String FILE = "file";
    private final static String PRINTER_PROP = "awt.print.printer";
    private final static String FILENAME_PROP = "awt.print.fileName";
    private final static String NUMCOPIES_PROP = "awt.print.numCopies";
    private final static String OPTIONS_PROP = "awt.print.options";
    private final static String ORIENT_PROP = "awt.print.orientation";
    private final static String PORTRAIT = "portrait";
    private final static String LANDSCAPE = "landscape";
    private final static String PAPERSIZE_PROP = "awt.print.paperSize";
    private final static String LETTER = "letter";
    private final static String LEGAL = "legal";
    private final static String EXECUTIVE = "executive";
    private final static String A4 = "a4";
    private Properties props;
    private String options = ""; 
    private Thread printerJobThread;
    public PrintJob2D(Frame frame,  String doctitle,
                      final Properties props) {
        this.props = props;
        this.jobAttributes = new JobAttributes();
        this.pageAttributes = new PageAttributes();
        translateInputProps();
        initPrintJob2D(frame, doctitle,
                       this.jobAttributes, this.pageAttributes);
    }
    public PrintJob2D(Frame frame,  String doctitle,
                      JobAttributes jobAttributes,
                      PageAttributes pageAttributes) {
        initPrintJob2D(frame, doctitle, jobAttributes, pageAttributes);
    }
    private void initPrintJob2D(Frame frame,  String doctitle,
                                JobAttributes jobAttributes,
                                PageAttributes pageAttributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        if (frame == null &&
            (jobAttributes == null ||
             jobAttributes.getDialog() == DialogType.NATIVE)) {
            throw new NullPointerException("Frame must not be null");
        }
        this.frame = frame;
        this.docTitle = (doctitle == null) ? "" : doctitle;
        this.jobAttributes = (jobAttributes != null)
            ? jobAttributes : new JobAttributes();
        this.pageAttributes = (pageAttributes != null)
            ? pageAttributes : new PageAttributes();
        int[][] pageRanges = this.jobAttributes.getPageRanges();
        int first = pageRanges[0][0];
        int last = pageRanges[pageRanges.length - 1][1];
        this.jobAttributes.setPageRanges(new int[][] {
            new int[] { first, last }
        });
        this.jobAttributes.setToPage(last);
        this.jobAttributes.setFromPage(first);
        int[] res = this.pageAttributes.getPrinterResolution();
        if (res[0] != res[1]) {
            throw new IllegalArgumentException("Differing cross feed and feed"+
                                               " resolutions not supported.");
        }
        DestinationType dest= this.jobAttributes.getDestination();
        if (dest == DestinationType.FILE) {
            throwPrintToFile();
            String destStr = jobAttributes.getFileName();
            if ((destStr != null) &&
                (jobAttributes.getDialog() == JobAttributes.DialogType.NONE)) {
                File f = new File(destStr);
                try {
                    if (f.createNewFile()) {
                        f.delete();
                    }
                } catch (IOException ioe) {
                    throw new IllegalArgumentException("Cannot write to file:"+
                                                       destStr);
                } catch (SecurityException se) {
                }
                 File pFile = f.getParentFile();
                 if ((f.exists() &&
                      (!f.isFile() || !f.canWrite())) ||
                     ((pFile != null) &&
                      (!pFile.exists() || (pFile.exists() && !pFile.canWrite())))) {
                     throw new IllegalArgumentException("Cannot write to file:"+
                                                        destStr);
                 }
            }
        }
    }
    public boolean printDialog() {
        boolean proceedWithPrint = false;
        printerJob = PrinterJob.getPrinterJob();
        if (printerJob == null) {
            return false;
        }
        DialogType d = this.jobAttributes.getDialog();
        PrintService pServ = printerJob.getPrintService();
        if ((pServ == null) &&  (d == DialogType.NONE)){
            return false;
        }
        copyAttributes(pServ);
        DefaultSelectionType select =
            this.jobAttributes.getDefaultSelection();
        if (select == DefaultSelectionType.RANGE) {
            attributes.add(SunPageSelection.RANGE);
        } else if (select == DefaultSelectionType.SELECTION) {
            attributes.add(SunPageSelection.SELECTION);
        } else {
            attributes.add(SunPageSelection.ALL);
        }
        if (frame != null) {
             attributes.add(new DialogOwner(frame));
         }
        if ( d == DialogType.NONE) {
            proceedWithPrint = true;
        } else {
            if (d == DialogType.NATIVE) {
                attributes.add(DialogTypeSelection.NATIVE);
            }  else { 
                attributes.add(DialogTypeSelection.COMMON);
            }
            if (proceedWithPrint = printerJob.printDialog(attributes)) {
                if (pServ == null) {
                    pServ = printerJob.getPrintService();
                    if (pServ == null) {
                        return false;
                    }
                }
                updateAttributes();
                translateOutputProps();
            }
        }
        if (proceedWithPrint) {
            JobName jname = (JobName)attributes.get(JobName.class);
            if (jname != null) {
                printerJob.setJobName(jname.toString());
            }
            pageFormat = new PageFormat();
            Media media = (Media)attributes.get(Media.class);
            MediaSize mediaSize =  null;
            if (media != null  && media instanceof MediaSizeName) {
                mediaSize = MediaSize.getMediaSizeForName((MediaSizeName)media);
            }
            Paper p = pageFormat.getPaper();
            if (mediaSize != null) {
                p.setSize(mediaSize.getX(MediaSize.INCH)*72.0,
                          mediaSize.getY(MediaSize.INCH)*72.0);
            }
            if (pageAttributes.getOrigin()==OriginType.PRINTABLE) {
                p.setImageableArea(18.0, 18.0,
                                   p.getWidth()-36.0,
                                   p.getHeight()-36.0);
            } else {
                p.setImageableArea(0.0,0.0,p.getWidth(),p.getHeight());
            }
            pageFormat.setPaper(p);
            OrientationRequested orient =
               (OrientationRequested)attributes.get(OrientationRequested.class);
            if (orient!= null &&
                orient == OrientationRequested.REVERSE_LANDSCAPE) {
                pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
            } else if (orient == OrientationRequested.LANDSCAPE) {
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            } else {
                pageFormat.setOrientation(PageFormat.PORTRAIT);
                }
            printerJob.setPrintable(this, pageFormat);
        }
        return proceedWithPrint;
    }
    private void updateAttributes() {
        Copies c = (Copies)attributes.get(Copies.class);
        jobAttributes.setCopies(c.getValue());
        SunPageSelection sel =
            (SunPageSelection)attributes.get(SunPageSelection.class);
        if (sel == SunPageSelection.RANGE) {
            jobAttributes.setDefaultSelection(DefaultSelectionType.RANGE);
        } else if (sel == SunPageSelection.SELECTION) {
            jobAttributes.setDefaultSelection(DefaultSelectionType.SELECTION);
        } else {
            jobAttributes.setDefaultSelection(DefaultSelectionType.ALL);
        }
        Destination dest = (Destination)attributes.get(Destination.class);
        if (dest != null) {
            jobAttributes.setDestination(DestinationType.FILE);
            jobAttributes.setFileName(dest.getURI().getPath());
        } else {
            jobAttributes.setDestination(DestinationType.PRINTER);
        }
        PrintService serv = printerJob.getPrintService();
        if (serv != null) {
            jobAttributes.setPrinter(serv.getName());
        }
        PageRanges range = (PageRanges)attributes.get(PageRanges.class);
        int[][] members = range.getMembers();
        jobAttributes.setPageRanges(members);
        SheetCollate collation =
            (SheetCollate)attributes.get(SheetCollate.class);
        if (collation == SheetCollate.COLLATED) {
            jobAttributes.setMultipleDocumentHandling(
            MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES);
        } else {
            jobAttributes.setMultipleDocumentHandling(
            MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
        }
        Sides sides = (Sides)attributes.get(Sides.class);
        if (sides == Sides.TWO_SIDED_LONG_EDGE) {
            jobAttributes.setSides(SidesType.TWO_SIDED_LONG_EDGE);
        } else if (sides == Sides.TWO_SIDED_SHORT_EDGE) {
            jobAttributes.setSides(SidesType.TWO_SIDED_SHORT_EDGE);
        } else {
            jobAttributes.setSides(SidesType.ONE_SIDED);
        }
        Chromaticity color =
            (Chromaticity)attributes.get(Chromaticity.class);
        if (color == Chromaticity.COLOR) {
            pageAttributes.setColor(ColorType.COLOR);
        } else {
            pageAttributes.setColor(ColorType.MONOCHROME);
        }
        OrientationRequested orient =
            (OrientationRequested)attributes.get(OrientationRequested.class);
        if (orient == OrientationRequested.LANDSCAPE) {
            pageAttributes.setOrientationRequested(
                                       OrientationRequestedType.LANDSCAPE);
        } else {
            pageAttributes.setOrientationRequested(
                                       OrientationRequestedType.PORTRAIT);
        }
        PrintQuality qual = (PrintQuality)attributes.get(PrintQuality.class);
        if (qual == PrintQuality.DRAFT) {
            pageAttributes.setPrintQuality(PrintQualityType.DRAFT);
        } else if (qual == PrintQuality.HIGH) {
            pageAttributes.setPrintQuality(PrintQualityType.HIGH);
        } else { 
            pageAttributes.setPrintQuality(PrintQualityType.NORMAL);
        }
        Media msn = (Media)attributes.get(Media.class);
        if (msn != null && msn instanceof MediaSizeName) {
            MediaType mType = unMapMedia((MediaSizeName)msn);
            if (mType != null) {
                pageAttributes.setMedia(mType);
            }
        }
        debugPrintAttributes(false, false);
    }
    private void debugPrintAttributes(boolean ja, boolean pa ) {
        if (ja) {
            System.out.println("new Attributes\ncopies = "+
                               jobAttributes.getCopies()+
                               "\nselection = "+
                               jobAttributes.getDefaultSelection()+
                               "\ndest "+jobAttributes.getDestination()+
                               "\nfile "+jobAttributes.getFileName()+
                               "\nfromPage "+jobAttributes.getFromPage()+
                               "\ntoPage "+jobAttributes.getToPage()+
                               "\ncollation "+
                               jobAttributes.getMultipleDocumentHandling()+
                               "\nPrinter "+jobAttributes.getPrinter()+
                               "\nSides2 "+jobAttributes.getSides()
                               );
        }
        if (pa) {
            System.out.println("new Attributes\ncolor = "+
                               pageAttributes.getColor()+
                               "\norientation = "+
                               pageAttributes.getOrientationRequested()+
                               "\nquality "+pageAttributes.getPrintQuality()+
                               "\nMedia2 "+pageAttributes.getMedia()
                               );
        }
    }
    private void copyAttributes(PrintService printServ) {
        attributes = new HashPrintRequestAttributeSet();
        attributes.add(new JobName(docTitle, null));
        PrintService pServ = printServ;
        String printerName = jobAttributes.getPrinter();
        if (printerName != null && printerName != ""
            && !printerName.equals(pServ.getName())) {
            PrintService []services = PrinterJob.lookupPrintServices();
            try {
                for (int i=0; i<services.length; i++) {
                    if (printerName.equals(services[i].getName())) {
                        printerJob.setPrintService(services[i]);
                        pServ = services[i];
                        break;
                    }
                }
            } catch (PrinterException pe) {
            }
        }
        DestinationType dest = jobAttributes.getDestination();
        if (dest == DestinationType.FILE &&
            pServ.isAttributeCategorySupported(Destination.class)) {
            String fileName = jobAttributes.getFileName();
            Destination defaultDest;
            if (fileName == null && (defaultDest = (Destination)pServ.
                    getDefaultAttributeValue(Destination.class)) != null) {
                attributes.add(defaultDest);
            } else {
                URI uri = null;
                try {
                    if (fileName != null) {
                        if (fileName.equals("")) {
                            fileName = ".";
                        }
                    } else {
                        fileName = "out.prn";
                    }
                    uri = (new File(fileName)).toURI();
                } catch (SecurityException se) {
                    try {
                        fileName = fileName.replace('\\', '/');
                        uri = new URI("file:"+fileName);
                    } catch (URISyntaxException e) {
                    }
                }
                if (uri != null) {
                    attributes.add(new Destination(uri));
                }
            }
        }
        attributes.add(new SunMinMaxPage(jobAttributes.getMinPage(),
                                         jobAttributes.getMaxPage()));
        SidesType sType = jobAttributes.getSides();
        if (sType == SidesType.TWO_SIDED_LONG_EDGE) {
            attributes.add(Sides.TWO_SIDED_LONG_EDGE);
        } else if (sType == SidesType.TWO_SIDED_SHORT_EDGE) {
            attributes.add(Sides.TWO_SIDED_SHORT_EDGE);
        } else if (sType == SidesType.ONE_SIDED) {
            attributes.add(Sides.ONE_SIDED);
        }
        MultipleDocumentHandlingType hType =
          jobAttributes.getMultipleDocumentHandling();
        if (hType ==
            MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES) {
          attributes.add(SheetCollate.COLLATED);
        } else {
          attributes.add(SheetCollate.UNCOLLATED);
        }
        attributes.add(new Copies(jobAttributes.getCopies()));
        attributes.add(new PageRanges(jobAttributes.getFromPage(),
                                      jobAttributes.getToPage()));
        if (pageAttributes.getColor() == ColorType.COLOR) {
            attributes.add(Chromaticity.COLOR);
        } else {
            attributes.add(Chromaticity.MONOCHROME);
        }
        pageFormat = printerJob.defaultPage();
        if (pageAttributes.getOrientationRequested() ==
            OrientationRequestedType.LANDSCAPE) {
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
                attributes.add(OrientationRequested.LANDSCAPE);
        } else {
                pageFormat.setOrientation(PageFormat.PORTRAIT);
                attributes.add(OrientationRequested.PORTRAIT);
        }
        MediaType media = pageAttributes.getMedia();
        MediaSizeName msn = mapMedia(media);
        if (msn != null) {
            attributes.add(msn);
        }
        PrintQualityType qType =
            pageAttributes.getPrintQuality();
        if (qType == PrintQualityType.DRAFT) {
            attributes.add(PrintQuality.DRAFT);
        } else if (qType == PrintQualityType.NORMAL) {
            attributes.add(PrintQuality.NORMAL);
        } else if (qType == PrintQualityType.HIGH) {
            attributes.add(PrintQuality.HIGH);
        }
    }
    public Graphics getGraphics() {
        Graphics printGraphics = null;
        synchronized (this) {
            ++pageIndex;
            if (pageIndex == 0 && !graphicsToBeDrawn.isClosed()) {
                startPrinterJobThread();
            }
            notify();
        }
        if (currentGraphics != null) {
            graphicsDrawn.append(currentGraphics);
            currentGraphics = null;
        }
        currentGraphics = graphicsToBeDrawn.pop();
        if (currentGraphics instanceof PeekGraphics) {
            ( (PeekGraphics) currentGraphics).setAWTDrawingOnly();
            graphicsDrawn.append(currentGraphics);
            currentGraphics = graphicsToBeDrawn.pop();
        }
        if (currentGraphics != null) {
            currentGraphics.translate(pageFormat.getImageableX(),
                                      pageFormat.getImageableY());
            double awtScale = 72.0/getPageResolutionInternal();
            currentGraphics.scale(awtScale, awtScale);
            printGraphics = new ProxyPrintGraphics(currentGraphics.create(),
                                                   this);
        }
        return printGraphics;
    }
    public Dimension getPageDimension() {
        double wid, hgt, scale;
        if (pageAttributes != null &&
            pageAttributes.getOrigin()==OriginType.PRINTABLE) {
            wid = pageFormat.getImageableWidth();
            hgt = pageFormat.getImageableHeight();
        } else {
            wid = pageFormat.getWidth();
            hgt = pageFormat.getHeight();
        }
        scale = getPageResolutionInternal() / 72.0;
        return new Dimension((int)(wid * scale), (int)(hgt * scale));
    }
     private double getPageResolutionInternal() {
        if (pageAttributes != null) {
            int []res = pageAttributes.getPrinterResolution();
            if (res[2] == 3) {
                return res[0];
            } else  {
                return (res[0] * 2.54);
            }
        } else {
            return 72.0;
        }
    }
    public int getPageResolution() {
        return (int)getPageResolutionInternal();
    }
    public boolean lastPageFirst() {
        return false;
    }
    public synchronized void end() {
        graphicsToBeDrawn.close();
        if (currentGraphics != null) {
            graphicsDrawn.append(currentGraphics);
        }
        graphicsDrawn.closeWhenEmpty();
        if( printerJobThread != null && printerJobThread.isAlive() ){
            try {
                printerJobThread.join();
            } catch (InterruptedException e) {
            }
        }
    }
    public void finalize() {
        end();
    }
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                 throws PrinterException {
        int result;
        graphicsToBeDrawn.append( (Graphics2D) graphics);
        if (graphicsDrawn.pop() != null) {
            result = PAGE_EXISTS;
        } else {
            result = NO_SUCH_PAGE;
        }
        return result;
    }
    private void startPrinterJobThread() {
        printerJobThread = new Thread(this, "printerJobThread");
        printerJobThread.start();
    }
    public void run() {
        try {
            printerJob.print(attributes);
        } catch (PrinterException e) {
        }
        graphicsToBeDrawn.closeWhenEmpty();
        graphicsDrawn.close();
    }
    private class MessageQ {
        private String qid="noname";
        private ArrayList queue = new ArrayList();
        MessageQ(String id) {
          qid = id;
        }
        synchronized void closeWhenEmpty() {
            while (queue != null && queue.size() > 0) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
            }
            queue = null;
            notifyAll();
        }
        synchronized void close() {
            queue = null;
            notifyAll();
        }
        synchronized boolean append(Graphics2D g) {
            boolean queued = false;
            if (queue != null) {
                queue.add(g);
                queued = true;
                notify();
            }
            return queued;
        }
        synchronized Graphics2D pop() {
            Graphics2D g = null;
            while (g == null && queue != null) {
                if (queue.size() > 0) {
                    g = (Graphics2D) queue.remove(0);
                    notify();
                } else {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            return g;
        }
        synchronized boolean isClosed() {
            return queue == null;
        }
    }
    private static int[] getSize(MediaType mType) {
        int []dim = new int[2];
        dim[0] = 612;
        dim[1] = 792;
        for (int i=0; i < SIZES.length; i++) {
            if (SIZES[i] == mType) {
                dim[0] = WIDTHS[i];
                dim[1] = LENGTHS[i];
                break;
            }
        }
        return dim;
    }
    public static MediaSizeName mapMedia(MediaType mType) {
        MediaSizeName media = null;
        int length = Math.min(SIZES.length, JAVAXSIZES.length);
        for (int i=0; i < length; i++) {
            if (SIZES[i] == mType) {
                if ((JAVAXSIZES[i] != null) &&
                    MediaSize.getMediaSizeForName(JAVAXSIZES[i]) != null) {
                    media = JAVAXSIZES[i];
                    break;
                } else {
                    media = new CustomMediaSizeName(SIZES[i].toString());
                    float w = (float)Math.rint(WIDTHS[i]  / 72.0);
                    float h = (float)Math.rint(LENGTHS[i] / 72.0);
                    if (w > 0.0 && h > 0.0) {
                        new MediaSize(w, h, Size2DSyntax.INCH, media);
                    }
                    break;
                }
            }
        }
        return media;
    }
    public static MediaType unMapMedia(MediaSizeName mSize) {
        MediaType media = null;
        int length = Math.min(SIZES.length, JAVAXSIZES.length);
        for (int i=0; i < length; i++) {
            if (JAVAXSIZES[i] == mSize) {
                if (SIZES[i] != null) {
                    media = SIZES[i];
                    break;
                }
            }
        }
        return media;
    }
    private void translateInputProps() {
        if (props == null) {
            return;
        }
        String str;
        str = props.getProperty(DEST_PROP);
        if (str != null) {
            if (str.equals(PRINTER)) {
                jobAttributes.setDestination(DestinationType.PRINTER);
            } else if (str.equals(FILE)) {
                jobAttributes.setDestination(DestinationType.FILE);
            }
        }
        str = props.getProperty(PRINTER_PROP);
        if (str != null) {
            jobAttributes.setPrinter(str);
        }
        str = props.getProperty(FILENAME_PROP);
        if (str != null) {
            jobAttributes.setFileName(str);
        }
        str = props.getProperty(NUMCOPIES_PROP);
        if (str != null) {
            jobAttributes.setCopies(Integer.parseInt(str));
        }
        this.options = props.getProperty(OPTIONS_PROP, "");
        str = props.getProperty(ORIENT_PROP);
        if (str != null) {
            if (str.equals(PORTRAIT)) {
                pageAttributes.setOrientationRequested(
                                        OrientationRequestedType.PORTRAIT);
            } else if (str.equals(LANDSCAPE)) {
                pageAttributes.setOrientationRequested(
                                        OrientationRequestedType.LANDSCAPE);
            }
        }
        str = props.getProperty(PAPERSIZE_PROP);
        if (str != null) {
            if (str.equals(LETTER)) {
                pageAttributes.setMedia(SIZES[MediaType.LETTER.hashCode()]);
            } else if (str.equals(LEGAL)) {
                pageAttributes.setMedia(SIZES[MediaType.LEGAL.hashCode()]);
            } else if (str.equals(EXECUTIVE)) {
                pageAttributes.setMedia(SIZES[MediaType.EXECUTIVE.hashCode()]);
            } else if (str.equals(A4)) {
                pageAttributes.setMedia(SIZES[MediaType.A4.hashCode()]);
            }
        }
    }
    private void translateOutputProps() {
        if (props == null) {
            return;
        }
        String str;
        props.setProperty(DEST_PROP,
            (jobAttributes.getDestination() == DestinationType.PRINTER) ?
                          PRINTER : FILE);
        str = jobAttributes.getPrinter();
        if (str != null && !str.equals("")) {
            props.setProperty(PRINTER_PROP, str);
        }
        str = jobAttributes.getFileName();
        if (str != null && !str.equals("")) {
            props.setProperty(FILENAME_PROP, str);
        }
        int copies = jobAttributes.getCopies();
        if (copies > 0) {
            props.setProperty(NUMCOPIES_PROP, "" + copies);
        }
        str = this.options;
        if (str != null && !str.equals("")) {
            props.setProperty(OPTIONS_PROP, str);
        }
        props.setProperty(ORIENT_PROP,
            (pageAttributes.getOrientationRequested() ==
             OrientationRequestedType.PORTRAIT)
                          ? PORTRAIT : LANDSCAPE);
        MediaType media = SIZES[pageAttributes.getMedia().hashCode()];
        if (media == MediaType.LETTER) {
            str = LETTER;
        } else if (media == MediaType.LEGAL) {
            str = LEGAL;
        } else if (media == MediaType.EXECUTIVE) {
            str = EXECUTIVE;
        } else if (media == MediaType.A4) {
            str = A4;
        } else {
            str = media.toString();
        }
        props.setProperty(PAPERSIZE_PROP, str);
    }
    private void throwPrintToFile() {
        SecurityManager security = System.getSecurityManager();
        FilePermission printToFilePermission = null;
        if (security != null) {
            if (printToFilePermission == null) {
                printToFilePermission =
                    new FilePermission("<<ALL FILES>>", "read,write");
            }
            security.checkPermission(printToFilePermission);
        }
    }
}
