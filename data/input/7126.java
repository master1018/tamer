public class WPrinterJob extends RasterPrinterJob implements DisposerTarget {
    protected static final long PS_ENDCAP_ROUND  = 0x00000000;
    protected static final long PS_ENDCAP_SQUARE   = 0x00000100;
    protected static final long PS_ENDCAP_FLAT   =   0x00000200;
    protected static final long PS_JOIN_ROUND   =    0x00000000;
    protected static final long PS_JOIN_BEVEL   =    0x00001000;
    protected static final long PS_JOIN_MITER   =    0x00002000;
    protected static final int POLYFILL_ALTERNATE = 1;
    protected static final int POLYFILL_WINDING = 2;
    private static final int MAX_WCOLOR = 255;
    private static final int SET_DUP_VERTICAL = 0x00000010;
    private static final int SET_DUP_HORIZONTAL = 0x00000020;
    private static final int SET_RES_HIGH = 0x00000040;
    private static final int SET_RES_LOW = 0x00000080;
    private static final int SET_COLOR = 0x00000200;
    private static final int SET_ORIENTATION = 0x00004000;
    private static final int PD_ALLPAGES = 0x00000000;
    private static final int PD_SELECTION = 0x00000001;
    private static final int PD_PAGENUMS = 0x00000002;
    private static final int PD_NOSELECTION = 0x00000004;
    private static final int PD_COLLATE = 0x00000010;
    private static final int PD_PRINTTOFILE = 0x00000020;
    private static final int DM_ORIENTATION = 0x00000001;
    private static final int DM_PRINTQUALITY = 0x00000400;
    private static final int DM_COLOR = 0x00000800;
    private static final int DM_DUPLEX = 0x00001000;
    private static final int MAX_UNKNOWN_PAGES = 9999;
     private boolean driverDoesMultipleCopies = false;
     private boolean driverDoesCollation = false;
     private boolean userRequestedCollation = false;
     private boolean noDefaultPrinter = false;
    static class HandleRecord implements DisposerRecord {
        private long mPrintDC;
        private long mPrintHDevMode;
        private long mPrintHDevNames;
        public void dispose() {
            WPrinterJob.deleteDC(mPrintDC, mPrintHDevMode, mPrintHDevNames);
        }
    }
    private HandleRecord handleRecord = new HandleRecord();
    private int mPrintPaperSize;
    private int mPrintXRes;   
    private int mPrintYRes;   
    private int mPrintPhysX;  
    private int mPrintPhysY;  
    private int mPrintWidth;  
    private int mPrintHeight; 
    private int mPageWidth;   
    private int mPageHeight;  
    private int mAttSides;
    private int mAttChromaticity;
    private int mAttXRes;
    private int mAttYRes;
    private int mAttQuality;
    private int mAttCollate;
    private int mAttCopies;
    private int mAttMediaSizeName;
    private int mAttMediaTray;
    private String mDestination = null;
    private Color mLastColor;
    private Color mLastTextColor;
    private String mLastFontFamily;
    private float mLastFontSize;
    private int mLastFontStyle;
    private int mLastRotation;
    private float mLastAwScale;
    private PrinterJob pjob;
    private java.awt.peer.ComponentPeer dialogOwnerPeer = null;
    static {
        Toolkit.getDefaultToolkit();
        initIDs();
        Win32FontManager.registerJREFontsForPrinting();
    }
    public WPrinterJob()
    {
        Disposer.addRecord(disposerReferent,
                           handleRecord = new HandleRecord());
        initAttributeMembers();
    }
    private Object disposerReferent = new Object();
    public Object getDisposerReferent() {
        return disposerReferent;
    }
    public PageFormat pageDialog(PageFormat page) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (getPrintService() instanceof StreamPrintService) {
            return super.pageDialog(page);
        }
        PageFormat pageClone = (PageFormat) page.clone();
        boolean result = false;
        WPageDialog dialog = new WPageDialog((Frame)null, this,
                                     pageClone, null);
        dialog.setRetVal(false);
        dialog.setVisible(true);
        result = dialog.getRetVal();
        dialog.dispose();
        if (result && (myService != null)) {
            String printerName = getNativePrintService();
            if (!myService.getName().equals(printerName)) {
                try {
                    setPrintService(Win32PrintServiceLookup.
                                    getWin32PrintLUS().
                                    getPrintServiceByName(printerName));
                } catch (PrinterException e) {
                }
            }
            updatePageAttributes(myService, pageClone);
            return pageClone;
        } else {
            return page;
        }
    }
    private boolean displayNativeDialog() {
        if (attributes == null) {
            return false;
        }
        DialogOwner dlgOwner = (DialogOwner)attributes.get(DialogOwner.class);
        Frame ownerFrame = (dlgOwner != null) ? dlgOwner.getOwner() : null;
        WPrintDialog dialog = new WPrintDialog(ownerFrame, this);
        dialog.setRetVal(false);
        dialog.setVisible(true);
        boolean prv = dialog.getRetVal();
        dialog.dispose();
        Destination dest =
                (Destination)attributes.get(Destination.class);
        if ((dest == null) || !prv){
                return prv;
        } else {
            String title = null;
            String strBundle = "sun.print.resources.serviceui";
            ResourceBundle rb = ResourceBundle.getBundle(strBundle);
            try {
                title = rb.getString("dialog.printtofile");
            } catch (MissingResourceException e) {
            }
            FileDialog fileDialog = new FileDialog(ownerFrame, title,
                                                   FileDialog.SAVE);
            URI destURI = dest.getURI();
            String pathName = (destURI != null) ?
                destURI.getSchemeSpecificPart() : null;
            if (pathName != null) {
               File file = new File(pathName);
               fileDialog.setFile(file.getName());
               File parent = file.getParentFile();
               if (parent != null) {
                   fileDialog.setDirectory(parent.getPath());
               }
            } else {
                fileDialog.setFile("out.prn");
            }
            fileDialog.setVisible(true);
            String fileName = fileDialog.getFile();
            if (fileName == null) {
                fileDialog.dispose();
                return false;
            }
            String fullName = fileDialog.getDirectory() + fileName;
            File f = new File(fullName);
            File pFile = f.getParentFile();
            while ((f.exists() &&
                      (!f.isFile() || !f.canWrite())) ||
                   ((pFile != null) &&
                      (!pFile.exists() || (pFile.exists() && !pFile.canWrite())))) {
                (new PrintToFileErrorDialog(ownerFrame,
                                ServiceDialog.getMsg("dialog.owtitle"),
                                ServiceDialog.getMsg("dialog.writeerror")+" "+fullName,
                                ServiceDialog.getMsg("button.ok"))).setVisible(true);
                fileDialog.setVisible(true);
                fileName = fileDialog.getFile();
                if (fileName == null) {
                    fileDialog.dispose();
                    return false;
                }
                fullName = fileDialog.getDirectory() + fileName;
                f = new File(fullName);
                pFile = f.getParentFile();
            }
            fileDialog.dispose();
            attributes.add(new Destination(f.toURI()));
            return true;
        }
    }
    public boolean printDialog() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        if (getPrintService() instanceof StreamPrintService) {
            return super.printDialog(attributes);
        }
        if (noDefaultPrinter == true) {
            return false;
        } else {
            return displayNativeDialog();
        }
    }
    public void setPrintService(PrintService service)
        throws PrinterException {
        super.setPrintService(service);
        if (service instanceof StreamPrintService) {
            return;
        }
        driverDoesMultipleCopies = false;
        driverDoesCollation = false;
        setNativePrintService(service.getName());
    }
    private native void setNativePrintService(String name)
        throws PrinterException;
    public PrintService getPrintService() {
        if (myService == null) {
            String printerName = getNativePrintService();
            if (printerName != null) {
                myService = Win32PrintServiceLookup.getWin32PrintLUS().
                    getPrintServiceByName(printerName);
                if (myService != null) {
                    return myService;
                }
            }
            myService = PrintServiceLookup.lookupDefaultPrintService();
            if (myService != null) {
                try {
                    setNativePrintService(myService.getName());
                } catch (Exception e) {
                    myService = null;
                }
            }
          }
          return myService;
    }
    private native String getNativePrintService();
    private void initAttributeMembers() {
            mAttSides = 0;
            mAttChromaticity = 0;
            mAttXRes = 0;
            mAttYRes = 0;
            mAttQuality = 0;
            mAttCollate = -1;
            mAttCopies = 0;
            mAttMediaTray = 0;
            mAttMediaSizeName = 0;
            mDestination = null;
    }
    protected void setAttributes(PrintRequestAttributeSet attributes)
        throws PrinterException {
        initAttributeMembers();
        super.setAttributes(attributes);
        mAttCopies = getCopiesInt();
        mDestination = destinationAttr;
        if (attributes == null) {
            return; 
        }
        Attribute[] attrs = attributes.toArray();
        for (int i=0; i< attrs.length; i++) {
            Attribute attr = attrs[i];
            try {
                 if (attr.getCategory() == Sides.class) {
                    setSidesAttrib(attr);
                }
                else if (attr.getCategory() == Chromaticity.class) {
                    setColorAttrib(attr);
                }
                else if (attr.getCategory() == PrinterResolution.class) {
                    setResolutionAttrib(attr);
                }
                else if (attr.getCategory() == PrintQuality.class) {
                    setQualityAttrib(attr);
                }
                else if (attr.getCategory() == SheetCollate.class) {
                    setCollateAttrib(attr);
                }  else if (attr.getCategory() == Media.class ||
                            attr.getCategory() == SunAlternateMedia.class) {
                    if (attr.getCategory() == SunAlternateMedia.class) {
                        Media media = (Media)attributes.get(Media.class);
                        if (media == null ||
                            !(media instanceof MediaTray)) {
                            attr = ((SunAlternateMedia)attr).getMedia();
                        }
                    }
                    if (attr instanceof MediaSizeName) {
                        setWin32MediaAttrib(attr);
                    }
                    if (attr instanceof MediaTray) {
                        setMediaTrayAttrib(attr);
                    }
                }
            } catch (ClassCastException e) {
            }
        }
    }
    private native void getDefaultPage(PageFormat page);
    public PageFormat defaultPage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        getDefaultPage(newPage);
        return newPage;
    }
    protected native void validatePaper(Paper origPaper, Paper newPaper );
    protected Graphics2D createPathGraphics(PeekGraphics peekGraphics,
                                            PrinterJob printerJob,
                                            Printable painter,
                                            PageFormat pageFormat,
                                            int pageIndex) {
        WPathGraphics pathGraphics;
        PeekMetrics metrics = peekGraphics.getMetrics();
       if (forcePDL == false && (forceRaster == true
                                  || metrics.hasNonSolidColors()
                                  || metrics.hasCompositing()
                                  )) {
            pathGraphics = null;
        } else {
            BufferedImage bufferedImage = new BufferedImage(8, 8,
                                            BufferedImage.TYPE_INT_RGB);
            Graphics2D bufferedGraphics = bufferedImage.createGraphics();
            boolean canRedraw = peekGraphics.getAWTDrawingOnly() == false;
            pathGraphics =  new WPathGraphics(bufferedGraphics, printerJob,
                                              painter, pageFormat, pageIndex,
                                              canRedraw);
        }
        return pathGraphics;
    }
    protected double getXRes() {
        if (mAttXRes != 0) {
            return mAttXRes;
        } else {
            return mPrintXRes;
        }
    }
    protected double getYRes() {
        if (mAttYRes != 0) {
            return mAttYRes;
        } else {
            return mPrintYRes;
        }
    }
    protected double getPhysicalPrintableX(Paper p) {
        return mPrintPhysX;
    }
    protected double getPhysicalPrintableY(Paper p) {
        return mPrintPhysY;
    }
    protected double getPhysicalPrintableWidth(Paper p) {
        return mPrintWidth;
    }
    protected double getPhysicalPrintableHeight(Paper p) {
        return mPrintHeight;
    }
    protected double getPhysicalPageWidth(Paper p) {
        return mPageWidth;
    }
    protected double getPhysicalPageHeight(Paper p) {
        return mPageHeight;
    }
    protected boolean isCollated() {
        return userRequestedCollation;
    }
    protected int getCollatedCopies() {
        debug_println("driverDoesMultipleCopies="+driverDoesMultipleCopies
                      +" driverDoesCollation="+driverDoesCollation);
        if  (super.isCollated() && !driverDoesCollation) {
            mAttCollate = 0;
            mAttCopies = 1;
            return getCopies();
        }
        return 1;
    }
    protected int getNoncollatedCopies() {
        if (driverDoesMultipleCopies || super.isCollated()) {
            return 1;
        } else {
            return getCopies();
        }
    }
    private long getPrintDC() {
        return handleRecord.mPrintDC;
    }
    private void setPrintDC(long mPrintDC) {
        handleRecord.mPrintDC = mPrintDC;
    }
    private long getDevMode() {
        return handleRecord.mPrintHDevMode;
    }
    private void setDevMode(long mPrintHDevMode) {
        handleRecord.mPrintHDevMode = mPrintHDevMode;
    }
    private long getDevNames() {
        return handleRecord.mPrintHDevNames;
    }
    private void setDevNames(long mPrintHDevNames) {
        handleRecord.mPrintHDevNames = mPrintHDevNames;
    }
    protected void beginPath() {
        beginPath(getPrintDC());
    }
    protected void endPath() {
        endPath(getPrintDC());
    }
    protected void closeFigure() {
        closeFigure(getPrintDC());
    }
    protected void fillPath() {
        fillPath(getPrintDC());
    }
    protected void moveTo(float x, float y) {
        moveTo(getPrintDC(), x, y);
    }
    protected void lineTo(float x, float y) {
        lineTo(getPrintDC(), x, y);
    }
    protected void polyBezierTo(float control1x, float control1y,
                                float control2x, float control2y,
                                float endX, float endY) {
        polyBezierTo(getPrintDC(), control1x, control1y,
                               control2x, control2y,
                               endX, endY);
    }
    protected void setPolyFillMode(int fillRule) {
        setPolyFillMode(getPrintDC(), fillRule);
    }
    protected void selectSolidBrush(Color color) {
        if (color.equals(mLastColor) == false) {
            mLastColor = color;
            float[] rgb = color.getRGBColorComponents(null);
            selectSolidBrush(getPrintDC(),
                             (int) (rgb[0] * MAX_WCOLOR),
                             (int) (rgb[1] * MAX_WCOLOR),
                             (int) (rgb[2] * MAX_WCOLOR));
        }
    }
    protected int getPenX() {
        return getPenX(getPrintDC());
    }
    protected int getPenY() {
        return getPenY(getPrintDC());
    }
    protected void selectClipPath() {
        selectClipPath(getPrintDC());
    }
    protected void frameRect(float x, float y, float width, float height) {
        frameRect(getPrintDC(), x, y, width, height);
    }
    protected void fillRect(float x, float y, float width, float height,
                            Color color) {
        float[] rgb = color.getRGBColorComponents(null);
        fillRect(getPrintDC(), x, y, width, height,
                 (int) (rgb[0] * MAX_WCOLOR),
                 (int) (rgb[1] * MAX_WCOLOR),
                 (int) (rgb[2] * MAX_WCOLOR));
    }
    protected void selectPen(float width, Color color) {
        float[] rgb = color.getRGBColorComponents(null);
        selectPen(getPrintDC(), width,
                  (int) (rgb[0] * MAX_WCOLOR),
                  (int) (rgb[1] * MAX_WCOLOR),
                  (int) (rgb[2] * MAX_WCOLOR));
    }
    protected boolean selectStylePen(int cap, int join, float width,
                                     Color color) {
        long endCap;
        long lineJoin;
        float[] rgb = color.getRGBColorComponents(null);
        switch(cap) {
        case BasicStroke.CAP_BUTT: endCap = PS_ENDCAP_FLAT; break;
        case BasicStroke.CAP_ROUND: endCap = PS_ENDCAP_ROUND; break;
        default:
        case BasicStroke.CAP_SQUARE: endCap = PS_ENDCAP_SQUARE; break;
        }
        switch(join) {
        case BasicStroke.JOIN_BEVEL:lineJoin = PS_JOIN_BEVEL; break;
        default:
        case BasicStroke.JOIN_MITER:lineJoin = PS_JOIN_MITER; break;
        case BasicStroke.JOIN_ROUND:lineJoin = PS_JOIN_ROUND; break;
        }
        return (selectStylePen(getPrintDC(), endCap, lineJoin, width,
                               (int) (rgb[0] * MAX_WCOLOR),
                               (int) (rgb[1] * MAX_WCOLOR),
                               (int) (rgb[2] * MAX_WCOLOR)));
    }
    protected boolean setFont(String family, float size, int style,
                              int rotation, float awScale) {
        boolean didSetFont = true;
        if (!family.equals(mLastFontFamily) ||
            size     != mLastFontSize       ||
            style    != mLastFontStyle      ||
            rotation != mLastRotation       ||
            awScale  != mLastAwScale) {
            didSetFont = setFont(getPrintDC(),
                                 family,
                                 size,
                                 (style & Font.BOLD) != 0,
                                 (style & Font.ITALIC) != 0,
                                 rotation, awScale);
            if (didSetFont) {
                mLastFontFamily   = family;
                mLastFontSize     = size;
                mLastFontStyle    = style;
                mLastRotation     = rotation;
                mLastAwScale      = awScale;
            }
        }
        return didSetFont;
    }
    protected void setTextColor(Color color) {
        if (color.equals(mLastTextColor) == false) {
            mLastTextColor = color;
            float[] rgb = color.getRGBColorComponents(null);
            setTextColor(getPrintDC(),
                         (int) (rgb[0] * MAX_WCOLOR),
                         (int) (rgb[1] * MAX_WCOLOR),
                         (int) (rgb[2] * MAX_WCOLOR));
        }
    }
    protected String removeControlChars(String str) {
        return super.removeControlChars(str);
    }
    protected void textOut(String str, float x, float y,
                           float[] positions) {
        String text = removeControlChars(str);
        assert (positions == null) || (text.length() == str.length());
        if (text.length() == 0) {
            return;
        }
        textOut(getPrintDC(), text, text.length(), false, x, y, positions);
    }
    protected void glyphsOut(int []glyphs, float x, float y,
                             float[] positions) {
        char[] glyphCharArray = new char[glyphs.length];
        for (int i=0;i<glyphs.length;i++) {
            glyphCharArray[i] = (char)(glyphs[i] & 0xffff);
        }
        String glyphStr = new String(glyphCharArray);
        textOut(getPrintDC(), glyphStr, glyphs.length, true, x, y, positions);
    }
    protected int getGDIAdvance(String text) {
        text = removeControlChars(text);
        if (text.length() == 0) {
            return 0;
        }
        return getGDIAdvance(getPrintDC(), text);
    }
    protected void drawImage3ByteBGR(byte[] image,
                                     float destX, float destY,
                                     float destWidth, float destHeight,
                                     float srcX, float srcY,
                                     float srcWidth, float srcHeight) {
        drawDIBImage(getPrintDC(), image,
                     destX, destY,
                     destWidth, destHeight,
                     srcX, srcY,
                     srcWidth, srcHeight,
                     24, null);
    }
    protected void drawDIBImage(byte[] image,
                                float destX, float destY,
                                float destWidth, float destHeight,
                                float srcX, float srcY,
                                float srcWidth, float srcHeight,
                                int sampleBitsPerPixel,
                                IndexColorModel icm) {
        int bitCount = 24;
        byte[] bmiColors = null;
        if (icm != null) {
            bitCount = sampleBitsPerPixel;
            bmiColors = new byte[(1<<icm.getPixelSize())*4];
            for (int i=0;i<icm.getMapSize(); i++) {
                bmiColors[i*4+0]=(byte)(icm.getBlue(i)&0xff);
                bmiColors[i*4+1]=(byte)(icm.getGreen(i)&0xff);
                bmiColors[i*4+2]=(byte)(icm.getRed(i)&0xff);
            }
        }
        drawDIBImage(getPrintDC(), image,
                     destX, destY,
                     destWidth, destHeight,
                     srcX, srcY,
                     srcWidth, srcHeight,
                     bitCount, bmiColors);
    }
    protected void startPage(PageFormat format, Printable painter,
                             int index, boolean paperChanged) {
        invalidateCachedState();
        deviceStartPage(format, painter, index, paperChanged);
    }
    protected void endPage(PageFormat format, Printable painter,
                           int index) {
        deviceEndPage(format, painter, index);
    }
    private void invalidateCachedState() {
        mLastColor = null;
        mLastTextColor = null;
        mLastFontFamily = null;
    }
    public void setCopies(int copies) {
        super.setCopies(copies);
        mAttCopies = copies;
        setNativeCopies(copies);
    }
    public native void setNativeCopies(int copies);
    private native boolean jobSetup(Pageable doc, boolean allowPrintToFile);
    protected native void initPrinter();
    private native boolean _startDoc(String dest, String jobName)
                                     throws PrinterException;
    protected void startDoc() throws PrinterException {
        if (!_startDoc(mDestination, getJobName())) {
            cancel();
        }
    }
    protected native void endDoc();
    protected native void abortDoc();
    private static native void deleteDC(long dc, long devmode, long devnames);
    protected native void deviceStartPage(PageFormat format, Printable painter,
                                          int index, boolean paperChanged);
    protected native void deviceEndPage(PageFormat format, Printable painter,
                                        int index);
    protected native void printBand(byte[] data, int x, int y,
                                    int width, int height);
    protected native void beginPath(long printDC);
    protected native void endPath(long printDC);
    protected native void closeFigure(long printDC);
    protected native void fillPath(long printDC);
    protected native void moveTo(long printDC, float x, float y);
    protected native void lineTo(long printDC, float x, float y);
    protected native void polyBezierTo(long printDC,
                                       float control1x, float control1y,
                                       float control2x, float control2y,
                                       float endX, float endY);
    protected native void setPolyFillMode(long printDC, int fillRule);
    protected native void selectSolidBrush(long printDC,
                                           int red, int green, int blue);
    protected native int getPenX(long printDC);
    protected native int getPenY(long printDC);
    protected native void selectClipPath(long printDC);
    protected native void frameRect(long printDC, float x, float y,
                                    float width, float height);
    protected native void fillRect(long printDC, float x, float y,
                                   float width, float height,
                                   int red, int green, int blue);
    protected native void selectPen(long printDC, float width,
                                    int red, int green, int blue);
    protected native boolean selectStylePen(long printDC, long cap,
                                            long join, float width,
                                            int red, int green, int blue);
    protected native boolean setFont(long printDC, String familyName,
                                     float fontSize,
                                     boolean bold,
                                     boolean italic,
                                     int rotation,
                                     float awScale);
    protected native void setTextColor(long printDC,
                                       int red, int green, int blue);
    protected native void textOut(long printDC, String text,
                                  int strlen, boolean glyphs,
                                  float x, float y, float[] positions);
    private native int getGDIAdvance(long printDC, String text);
    private native void drawDIBImage(long printDC, byte[] image,
                                     float destX, float destY,
                                     float destWidth, float destHeight,
                                     float srcX, float srcY,
                                     float srcWidth, float srcHeight,
                                     int bitCount, byte[] bmiColors);
    private final String getPrinterAttrib() {
        PrintService service = this.getPrintService();
        String name = (service != null) ? service.getName() : null;
        return name;
    }
    private final boolean getCollateAttrib() {
        return (mAttCollate == 1);
    }
    private void setCollateAttrib(Attribute attr) {
        if (attr == SheetCollate.COLLATED) {
            mAttCollate = 1; 
        } else {
            mAttCollate = 0; 
        }
    }
    private void setCollateAttrib(Attribute attr,
                                  PrintRequestAttributeSet set) {
        setCollateAttrib(attr);
        set.add(attr);
    }
    private final int getOrientAttrib() {
        int orient = PageFormat.PORTRAIT;
        OrientationRequested orientReq = (attributes == null) ? null :
            (OrientationRequested)attributes.get(OrientationRequested.class);
        if (orientReq != null) {
            if (orientReq == OrientationRequested.REVERSE_LANDSCAPE) {
                orient = PageFormat.REVERSE_LANDSCAPE;
            } else if (orientReq == OrientationRequested.LANDSCAPE) {
                orient = PageFormat.LANDSCAPE;
            }
        }
        return orient;
    }
    private void setOrientAttrib(Attribute attr,
                                 PrintRequestAttributeSet set) {
        if (set != null) {
            set.add(attr);
        }
    }
    private final int getCopiesAttrib() {
        return getCopiesInt();
     }
    private final void setRangeCopiesAttribute(int from, int to,
                                               boolean isRangeSet,
                                               int copies) {
        if (attributes != null) {
            if (isRangeSet) {
                attributes.add(new PageRanges(from, to));
                setPageRange(from, to);
            }
            attributes.add(new Copies(copies));
            super.setCopies(copies);
            mAttCopies = copies;
        }
    }
    private final int getFromPageAttrib() {
        if (attributes != null) {
            PageRanges pageRangesAttr =
                (PageRanges)attributes.get(PageRanges.class);
            if (pageRangesAttr != null) {
                int[][] range = pageRangesAttr.getMembers();
                return range[0][0];
            }
        }
        return getMinPageAttrib();
    }
    private final int getToPageAttrib() {
        if (attributes != null) {
            PageRanges pageRangesAttr =
                (PageRanges)attributes.get(PageRanges.class);
            if (pageRangesAttr != null) {
                int[][] range = pageRangesAttr.getMembers();
                return range[range.length-1][1];
            }
        }
        return getMaxPageAttrib();
    }
    private final int getMinPageAttrib() {
        if (attributes != null) {
            SunMinMaxPage s =
                (SunMinMaxPage)attributes.get(SunMinMaxPage.class);
            if (s != null) {
                return s.getMin();
            }
        }
        return 1;
    }
    private final int getMaxPageAttrib() {
        if (attributes != null) {
            SunMinMaxPage s =
                (SunMinMaxPage)attributes.get(SunMinMaxPage.class);
            if (s != null) {
                return s.getMax();
            }
        }
        Pageable pageable = getPageable();
        if (pageable != null) {
            int numPages = pageable.getNumberOfPages();
            if (numPages <= Pageable.UNKNOWN_NUMBER_OF_PAGES) {
                numPages = MAX_UNKNOWN_PAGES;
            }
            return  ((numPages == 0) ? 1 : numPages);
        }
        return Integer.MAX_VALUE;
    }
    private final boolean getDestAttrib() {
        return (mDestination != null);
    }
    private final int getQualityAttrib() {
        return mAttQuality;
    }
    private void setQualityAttrib(Attribute attr) {
        if (attr == PrintQuality.HIGH) {
            mAttQuality = -4; 
        } else if (attr == PrintQuality.NORMAL) {
            mAttQuality = -3; 
        } else {
            mAttQuality = -2; 
        }
    }
    private void setQualityAttrib(Attribute attr,
                                  PrintRequestAttributeSet set) {
        setQualityAttrib(attr);
        set.add(attr);
    }
    private final int getColorAttrib() {
        return mAttChromaticity;
    }
    private void setColorAttrib(Attribute attr) {
        if (attr == Chromaticity.COLOR) {
            mAttChromaticity = 2; 
        } else {
            mAttChromaticity = 1; 
        }
    }
    private void setColorAttrib(Attribute attr,
                                  PrintRequestAttributeSet set) {
        setColorAttrib(attr);
        set.add(attr);
    }
    private final int getSidesAttrib() {
        return mAttSides;
    }
    private void setSidesAttrib(Attribute attr) {
        if (attr == Sides.TWO_SIDED_LONG_EDGE) {
            mAttSides = 2; 
        } else if (attr == Sides.TWO_SIDED_SHORT_EDGE) {
            mAttSides = 3; 
        } else { 
            mAttSides = 1;
        }
    }
    private void setSidesAttrib(Attribute attr,
                                PrintRequestAttributeSet set) {
        setSidesAttrib(attr);
        set.add(attr);
    }
    private final int[] getWin32MediaAttrib() {
        int wid_ht[] = {0, 0};
        if (attributes != null) {
            Media media = (Media)attributes.get(Media.class);
            if (media instanceof MediaSizeName) {
                MediaSizeName msn = (MediaSizeName)media;
                MediaSize ms = MediaSize.getMediaSizeForName(msn);
                if (ms != null) {
                    wid_ht[0] = (int)(ms.getX(MediaSize.INCH) * 72.0);
                    wid_ht[1] = (int)(ms.getY(MediaSize.INCH) * 72.0);
                }
            }
        }
        return wid_ht;
    }
    private void setWin32MediaAttrib(Attribute attr) {
        if (!(attr instanceof MediaSizeName)) {
            return;
        }
        MediaSizeName msn = (MediaSizeName)attr;
        mAttMediaSizeName = ((Win32PrintService)myService).findPaperID(msn);
    }
    private void setWin32MediaAttrib(int dmIndex, int width, int length) {
       MediaSizeName msn =
           ((Win32PrintService)myService).findWin32Media(dmIndex);
        if (msn == null) {
            msn = ((Win32PrintService)myService).
                findMatchingMediaSizeNameMM((float)width, (float)length);
        }
        if (msn != null) {
            if (attributes != null) {
                attributes.add(msn);
            }
        }
        mAttMediaSizeName = dmIndex;
    }
    private void setMediaTrayAttrib(Attribute attr) {
        if (attr == MediaTray.BOTTOM) {
            mAttMediaTray = 2;    
        } else if (attr == MediaTray.ENVELOPE) {
            mAttMediaTray = 5;    
        } else if (attr == MediaTray.LARGE_CAPACITY) {
            mAttMediaTray = 11;      
        } else if (attr == MediaTray.MAIN) {
            mAttMediaTray =1;               
        } else if (attr == MediaTray.MANUAL) {
            mAttMediaTray = 4;              
        } else if (attr == MediaTray.MIDDLE) {
            mAttMediaTray = 3;              
        } else if (attr == MediaTray.SIDE) {
            mAttMediaTray = 7;              
        } else if (attr == MediaTray.TOP) {
            mAttMediaTray =1;               
        } else {
            if (attr instanceof Win32MediaTray) {
                mAttMediaTray = ((Win32MediaTray)attr).winID;
            } else {
                mAttMediaTray = 1;  
            }
        }
    }
    private void setMediaTrayAttrib(int dmBinID) {
        mAttMediaTray = dmBinID;
        MediaTray tray = ((Win32PrintService)myService).findMediaTray(dmBinID);
    }
    private int getMediaTrayAttrib() {
        return mAttMediaTray;
    }
    private final int getSelectAttrib() {
        if (attributes != null) {
            SunPageSelection pages =
                (SunPageSelection)attributes.get(SunPageSelection.class);
            if (pages == SunPageSelection.RANGE) {
                return PD_PAGENUMS;
            } else if (pages == SunPageSelection.SELECTION) {
                return PD_SELECTION;
            } else if (pages ==  SunPageSelection.ALL) {
                return PD_ALLPAGES;
            }
        }
        return PD_NOSELECTION;
    }
    private final boolean getPrintToFileEnabled() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            FilePermission printToFilePermission =
                new FilePermission("<<ALL FILES>>", "read,write");
            try {
                security.checkPermission(printToFilePermission);
            } catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }
    private final void setNativeAttributes(int flags, int fields, int values) {
        if (attributes == null) {
            return;
        }
        if ((flags & PD_PRINTTOFILE) != 0) {
            Destination destPrn = (Destination)attributes.get(
                                                 Destination.class);
            if (destPrn == null) {
                try {
                    attributes.add(new Destination(
                                               new File("./out.prn").toURI()));
                } catch (SecurityException se) {
                    try {
                        attributes.add(new Destination(
                                                new URI("file:out.prn")));
                    } catch (URISyntaxException e) {
                    }
                }
            }
        } else {
            attributes.remove(Destination.class);
        }
        if ((flags & PD_COLLATE) != 0) {
            setCollateAttrib(SheetCollate.COLLATED, attributes);
        } else {
            setCollateAttrib(SheetCollate.UNCOLLATED, attributes);
        }
        if ((flags & PD_PAGENUMS) != 0) {
            attributes.add(SunPageSelection.RANGE);
        } else if ((flags & PD_SELECTION) != 0) {
            attributes.add(SunPageSelection.SELECTION);
        } else {
            attributes.add(SunPageSelection.ALL);
        }
        if ((fields & DM_ORIENTATION) != 0) {
            if ((values & SET_ORIENTATION) != 0) {
                setOrientAttrib(OrientationRequested.LANDSCAPE, attributes);
            } else {
                setOrientAttrib(OrientationRequested.PORTRAIT, attributes);
            }
        }
        if ((fields & DM_COLOR) != 0) {
            if ((values & SET_COLOR) != 0) {
                setColorAttrib(Chromaticity.COLOR, attributes);
            } else {
                setColorAttrib(Chromaticity.MONOCHROME, attributes);
            }
        }
        if ((fields & DM_PRINTQUALITY) != 0) {
            PrintQuality quality;
            if ((values & SET_RES_LOW) != 0) {
                quality = PrintQuality.DRAFT;
            } else if ((fields & SET_RES_HIGH) != 0) {
                quality = PrintQuality.HIGH;
            } else {
                quality = PrintQuality.NORMAL;
            }
            setQualityAttrib(quality, attributes);
        }
        if ((fields & DM_DUPLEX) != 0) {
            Sides sides;
            if ((values & SET_DUP_VERTICAL) != 0) {
                sides = Sides.TWO_SIDED_LONG_EDGE;
            } else if ((values & SET_DUP_HORIZONTAL) != 0) {
                sides = Sides.TWO_SIDED_SHORT_EDGE;
            } else {
                sides = Sides.ONE_SIDED;
            }
            setSidesAttrib(sides, attributes);
        }
    }
    private final void setResolutionDPI(int xres, int yres) {
        if (attributes != null) {
            PrinterResolution res =
                new PrinterResolution(xres, yres, PrinterResolution.DPI);
            attributes.add(res);
        }
        mAttXRes = xres;
        mAttYRes = yres;
    }
    private void setResolutionAttrib(Attribute attr) {
        PrinterResolution pr = (PrinterResolution)attr;
        mAttXRes = pr.getCrossFeedResolution(PrinterResolution.DPI);
        mAttYRes = pr.getFeedResolution(PrinterResolution.DPI);
    }
    private void setPrinterNameAttrib(String printerName) {
        PrintService service = this.getPrintService();
        if (printerName == null) {
            return;
        }
        if (service != null && printerName.equals(service.getName())) {
            return;
        } else {
            PrintService []services = PrinterJob.lookupPrintServices();
            for (int i=0; i<services.length; i++) {
                if (printerName.equals(services[i].getName())) {
                    try {
                        this.setPrintService(services[i]);
                    } catch (PrinterException e) {
                    }
                    return;
                }
            }
        }
   }
class PrintToFileErrorDialog extends Dialog implements ActionListener{
    public PrintToFileErrorDialog(Frame parent, String title, String message,
                           String buttonText) {
        super(parent, title, true);
        init (parent, title, message, buttonText);
    }
    public PrintToFileErrorDialog(Dialog parent, String title, String message,
                           String buttonText) {
        super(parent, title, true);
        init (parent, title, message, buttonText);
    }
    private void init(Component parent, String  title, String message,
                      String buttonText) {
        Panel p = new Panel();
        add("Center", new Label(message));
        Button btn = new Button(buttonText);
        btn.addActionListener(this);
        p.add(btn);
        add("South", p);
        pack();
        Dimension dDim = getSize();
        if (parent != null) {
            Rectangle fRect = parent.getBounds();
            setLocation(fRect.x + ((fRect.width - dDim.width) / 2),
                        fRect.y + ((fRect.height - dDim.height) / 2));
        }
    }
    public void actionPerformed(ActionEvent event) {
        setVisible(false);
        dispose();
        return;
    }
}
    private static native void initIDs();
}
