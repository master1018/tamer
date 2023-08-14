public class PSPrinterJob extends RasterPrinterJob {
    protected static final int FILL_EVEN_ODD = 1;
    protected static final int FILL_WINDING = 2;
    private static final int MAX_PSSTR = (1024 * 64 - 1);
    private static final int RED_MASK = 0x00ff0000;
    private static final int GREEN_MASK = 0x0000ff00;
    private static final int BLUE_MASK = 0x000000ff;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BLUE_SHIFT = 0;
    private static final int LOWNIBBLE_MASK = 0x0000000f;
    private static final int HINIBBLE_MASK =  0x000000f0;
    private static final int HINIBBLE_SHIFT = 4;
    private static final byte hexDigits[] = {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3',
        (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'A', (byte)'B',
        (byte)'C', (byte)'D', (byte)'E', (byte)'F'
    };
    private static final int PS_XRES = 300;
    private static final int PS_YRES = 300;
    private static final String ADOBE_PS_STR =  "%!PS-Adobe-3.0";
    private static final String EOF_COMMENT =   "%%EOF";
    private static final String PAGE_COMMENT =  "%%Page: ";
    private static final String READIMAGEPROC = "/imStr 0 def /imageSrc " +
        "{currentfile /ASCII85Decode filter /RunLengthDecode filter " +
        " imStr readstring pop } def";
    private static final String COPIES =        "/#copies exch def";
    private static final String PAGE_SAVE =     "/pgSave save def";
    private static final String PAGE_RESTORE =  "pgSave restore";
    private static final String SHOWPAGE =      "showpage";
    private static final String IMAGE_SAVE =    "/imSave save def";
    private static final String IMAGE_STR =     " string /imStr exch def";
    private static final String IMAGE_RESTORE = "imSave restore";
    private static final String COORD_PREP =    " 0 exch translate "
                                              + "1 -1 scale"
                                              + "[72 " + PS_XRES + " div "
                                              + "0 0 "
                                              + "72 " + PS_YRES + " div "
                                              + "0 0]concat";
    private static final String SetFontName = "F";
    private static final String DrawStringName = "S";
    private static final String EVEN_ODD_FILL_STR = "EF";
    private static final String WINDING_FILL_STR = "WF";
    private static final String EVEN_ODD_CLIP_STR = "EC";
    private static final String WINDING_CLIP_STR = "WC";
    private static final String MOVETO_STR = " M";
    private static final String LINETO_STR = " L";
    private static final String CURVETO_STR = " C";
    private static final String GRESTORE_STR = "R";
    private static final String GSAVE_STR = "G";
    private static final String NEWPATH_STR = "N";
    private static final String CLOSEPATH_STR = "P";
    private static final String SETRGBCOLOR_STR = " SC";
    private static final String SETGRAY_STR = " SG";
   private int mDestType;
   private String mDestination = "lp";
   private boolean mNoJobSheet = false;
   private String mOptions;
   private Font mLastFont;
   private Color mLastColor;
   private Shape mLastClip;
   private AffineTransform mLastTransform;
   private EPSPrinter epsPrinter = null;
   FontMetrics mCurMetrics;
   PrintStream mPSStream;
   File spoolFile;
    private String mFillOpStr = WINDING_FILL_STR;
    private String mClipOpStr = WINDING_CLIP_STR;
   ArrayList mGStateStack = new ArrayList();
   private float mPenX;
   private float mPenY;
   private float mStartPathX;
   private float mStartPathY;
   private static Properties mFontProps = null;
    static {
        java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction() {
            public Object run() {
                mFontProps = initProps();
                return null;
            }
        });
    }
    private static Properties initProps() {
        String jhome = System.getProperty("java.home");
        if (jhome != null){
            String ulocale = SunToolkit.getStartupLocale().getLanguage();
            try {
                File f = new File(jhome + File.separator +
                                  "lib" + File.separator +
                                  "psfontj2d.properties." + ulocale);
                if (!f.canRead()){
                    f = new File(jhome + File.separator +
                                      "lib" + File.separator +
                                      "psfont.properties." + ulocale);
                    if (!f.canRead()){
                        f = new File(jhome + File.separator + "lib" +
                                     File.separator + "psfontj2d.properties");
                        if (!f.canRead()){
                            f = new File(jhome + File.separator + "lib" +
                                         File.separator + "psfont.properties");
                            if (!f.canRead()){
                                return (Properties)null;
                            }
                        }
                    }
                }
                InputStream in =
                    new BufferedInputStream(new FileInputStream(f.getPath()));
                Properties props = new Properties();
                props.load(in);
                in.close();
                return props;
            } catch (Exception e){
                return (Properties)null;
            }
        }
        return (Properties)null;
    }
    public PSPrinterJob()
    {
    }
    public boolean printDialog() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        attributes.add(new Copies(getCopies()));
        attributes.add(new JobName(getJobName(), null));
        boolean doPrint = false;
        DialogTypeSelection dts =
            (DialogTypeSelection)attributes.get(DialogTypeSelection.class);
        if (dts == DialogTypeSelection.NATIVE) {
            attributes.remove(DialogTypeSelection.class);
            doPrint = printDialog(attributes);
            attributes.add(DialogTypeSelection.NATIVE);
        } else {
            doPrint = printDialog(attributes);
        }
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
                    mDestination = "out.ps";
                }
            } else {
                mDestType = RasterPrinterJob.PRINTER;
                PrintService pServ = getPrintService();
                if (pServ != null) {
                    mDestination = pServ.getName();
                }
            }
        }
        return doPrint;
    }
    protected void startDoc() throws PrinterException {
        OutputStream output;
        if (epsPrinter == null) {
            if (getPrintService() instanceof PSStreamPrintService) {
                StreamPrintService sps = (StreamPrintService)getPrintService();
                mDestType = RasterPrinterJob.STREAM;
                if (sps.isDisposed()) {
                    throw new PrinterException("service is disposed");
                }
                output = sps.getOutputStream();
                if (output == null) {
                    throw new PrinterException("Null output stream");
                }
            } else {
                mNoJobSheet = super.noJobSheet;
                if (super.destinationAttr != null) {
                    mDestType = RasterPrinterJob.FILE;
                    mDestination = super.destinationAttr;
                }
                if (mDestType == RasterPrinterJob.FILE) {
                    try {
                        spoolFile = new File(mDestination);
                        output =  new FileOutputStream(spoolFile);
                    } catch (IOException ex) {
                        throw new PrinterIOException(ex);
                    }
                } else {
                    PrinterOpener po = new PrinterOpener();
                    java.security.AccessController.doPrivileged(po);
                    if (po.pex != null) {
                        throw po.pex;
                    }
                    output = po.result;
                }
            }
            mPSStream = new PrintStream(new BufferedOutputStream(output));
            mPSStream.println(ADOBE_PS_STR);
        }
        mPSStream.println("%%BeginProlog");
        mPSStream.println(READIMAGEPROC);
        mPSStream.println("/BD {bind def} bind def");
        mPSStream.println("/D {def} BD");
        mPSStream.println("/C {curveto} BD");
        mPSStream.println("/L {lineto} BD");
        mPSStream.println("/M {moveto} BD");
        mPSStream.println("/R {grestore} BD");
        mPSStream.println("/G {gsave} BD");
        mPSStream.println("/N {newpath} BD");
        mPSStream.println("/P {closepath} BD");
        mPSStream.println("/EC {eoclip} BD");
        mPSStream.println("/WC {clip} BD");
        mPSStream.println("/EF {eofill} BD");
        mPSStream.println("/WF {fill} BD");
        mPSStream.println("/SG {setgray} BD");
        mPSStream.println("/SC {setrgbcolor} BD");
        mPSStream.println("/ISOF {");
        mPSStream.println("     dup findfont dup length 1 add dict begin {");
        mPSStream.println("             1 index /FID eq {pop pop} {D} ifelse");
        mPSStream.println("     } forall /Encoding ISOLatin1Encoding D");
        mPSStream.println("     currentdict end definefont");
        mPSStream.println("} BD");
        mPSStream.println("/NZ {dup 1 lt {pop 1} if} BD");
        mPSStream.println("/"+DrawStringName +" {");
        mPSStream.println("     moveto 1 index stringwidth pop NZ sub");
        mPSStream.println("     1 index length 1 sub NZ div 0");
        mPSStream.println("     3 2 roll ashow newpath} BD");
        mPSStream.println("/FL [");
        if (mFontProps == null){
            mPSStream.println(" /Helvetica ISOF");
            mPSStream.println(" /Helvetica-Bold ISOF");
            mPSStream.println(" /Helvetica-Oblique ISOF");
            mPSStream.println(" /Helvetica-BoldOblique ISOF");
            mPSStream.println(" /Times-Roman ISOF");
            mPSStream.println(" /Times-Bold ISOF");
            mPSStream.println(" /Times-Italic ISOF");
            mPSStream.println(" /Times-BoldItalic ISOF");
            mPSStream.println(" /Courier ISOF");
            mPSStream.println(" /Courier-Bold ISOF");
            mPSStream.println(" /Courier-Oblique ISOF");
            mPSStream.println(" /Courier-BoldOblique ISOF");
        } else {
            int cnt = Integer.parseInt(mFontProps.getProperty("font.num", "9"));
            for (int i = 0; i < cnt; i++){
                mPSStream.println("    /" + mFontProps.getProperty
                           ("font." + String.valueOf(i), "Courier ISOF"));
            }
        }
        mPSStream.println("] D");
        mPSStream.println("/"+SetFontName +" {");
        mPSStream.println("     FL exch get exch scalefont");
        mPSStream.println("     [1 0 0 -1 0 0] makefont setfont} BD");
        mPSStream.println("%%EndProlog");
        mPSStream.println("%%BeginSetup");
        if (epsPrinter == null) {
            PageFormat pageFormat = getPageable().getPageFormat(0);
            double paperHeight = pageFormat.getPaper().getHeight();
            double paperWidth = pageFormat.getPaper().getWidth();
            mPSStream.print("<< /PageSize [" +
                                           paperWidth + " "+ paperHeight+"]");
            final PrintService pservice = getPrintService();
            Boolean isPS = (Boolean)java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                       try {
                           Class psClass = Class.forName("sun.print.IPPPrintService");
                           if (psClass.isInstance(pservice)) {
                               Method isPSMethod = psClass.getMethod("isPostscript",
                                                                     (Class[])null);
                               return (Boolean)isPSMethod.invoke(pservice, (Object[])null);
                           }
                       } catch (Throwable t) {
                       }
                       return Boolean.TRUE;
                    }
                }
            );
            if (isPS) {
                mPSStream.print(" /DeferredMediaSelection true");
            }
            mPSStream.print(" /ImagingBBox null /ManualFeed false");
            mPSStream.print(isCollated() ? " /Collate true":"");
            mPSStream.print(" /NumCopies " +getCopiesInt());
            if (sidesAttr != Sides.ONE_SIDED) {
                if (sidesAttr == Sides.TWO_SIDED_LONG_EDGE) {
                    mPSStream.print(" /Duplex true ");
                } else if (sidesAttr == Sides.TWO_SIDED_SHORT_EDGE) {
                    mPSStream.print(" /Duplex true /Tumble true ");
                }
            }
            mPSStream.println(" >> setpagedevice ");
        }
        mPSStream.println("%%EndSetup");
    }
    private class PrinterOpener implements java.security.PrivilegedAction {
        PrinterException pex;
        OutputStream result;
        public Object run() {
            try {
                    spoolFile = File.createTempFile("javaprint", ".ps", null);
                    spoolFile.deleteOnExit();
                result = new FileOutputStream(spoolFile);
                return result;
            } catch (IOException ex) {
                pex = new PrinterIOException(ex);
            }
            return null;
        }
    }
    private class PrinterSpooler implements java.security.PrivilegedAction {
        PrinterException pex;
        public Object run() {
            try {
                if (spoolFile == null || !spoolFile.exists()) {
                   pex = new PrinterException("No spool file");
                   return null;
                }
                String fileName = spoolFile.getAbsolutePath();
                String execCmd[] = printExecCmd(mDestination, mOptions,
                               mNoJobSheet, getJobNameInt(),
                                                1, fileName);
                Process process = Runtime.getRuntime().exec(execCmd);
                process.waitFor();
                spoolFile.delete();
            } catch (IOException ex) {
                pex = new PrinterIOException(ex);
            } catch (InterruptedException ie) {
                pex = new PrinterException(ie.toString());
            }
            return null;
        }
    }
    protected void abortDoc() {
        if (mPSStream != null && mDestType != RasterPrinterJob.STREAM) {
            mPSStream.close();
        }
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
               if (spoolFile != null && spoolFile.exists()) {
                   spoolFile.delete();
               }
               return null;
            }
        });
    }
    protected void endDoc() throws PrinterException {
        if (mPSStream != null) {
            mPSStream.println(EOF_COMMENT);
            mPSStream.flush();
            if (mDestType != RasterPrinterJob.STREAM) {
                mPSStream.close();
            }
        }
        if (mDestType == RasterPrinterJob.PRINTER) {
            if (getPrintService() != null) {
                mDestination = getPrintService().getName();
            }
            PrinterSpooler spooler = new PrinterSpooler();
            java.security.AccessController.doPrivileged(spooler);
            if (spooler.pex != null) {
                throw spooler.pex;
            }
        }
    }
    protected void startPage(PageFormat pageFormat, Printable painter,
                             int index, boolean paperChanged)
        throws PrinterException
    {
        double paperHeight = pageFormat.getPaper().getHeight();
        double paperWidth = pageFormat.getPaper().getWidth();
        int pageNumber = index + 1;
        mGStateStack = new ArrayList();
        mGStateStack.add(new GState());
        mPSStream.println(PAGE_COMMENT + pageNumber + " " + pageNumber);
        if (index > 0 && paperChanged) {
            mPSStream.print("<< /PageSize [" +
                            paperWidth + " " + paperHeight + "]");
            final PrintService pservice = getPrintService();
            Boolean isPS =
                (Boolean)java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            Class psClass =
                                Class.forName("sun.print.IPPPrintService");
                            if (psClass.isInstance(pservice)) {
                                Method isPSMethod =
                                    psClass.getMethod("isPostscript",
                                                      (Class[])null);
                                return (Boolean)
                                    isPSMethod.invoke(pservice,
                                                      (Object[])null);
                            }
                        } catch (Throwable t) {
                        }
                        return Boolean.TRUE;
                    }
                    }
                );
            if (isPS) {
                mPSStream.print(" /DeferredMediaSelection true");
            }
            mPSStream.println(" >> setpagedevice");
        }
        mPSStream.println(PAGE_SAVE);
        mPSStream.println(paperHeight + COORD_PREP);
    }
    protected void endPage(PageFormat format, Printable painter,
                           int index)
        throws PrinterException
    {
        mPSStream.println(PAGE_RESTORE);
        mPSStream.println(SHOWPAGE);
    }
    protected void drawImageBGR(byte[] bgrData,
                                   float destX, float destY,
                                   float destWidth, float destHeight,
                                   float srcX, float srcY,
                                   float srcWidth, float srcHeight,
                                   int srcBitMapWidth, int srcBitMapHeight) {
        setTransform(new AffineTransform());
        prepDrawing();
        int intSrcWidth = (int) srcWidth;
        int intSrcHeight = (int) srcHeight;
        mPSStream.println(IMAGE_SAVE);
        int psBytesPerRow = 3 * (int) intSrcWidth;
        while (psBytesPerRow > MAX_PSSTR) {
            psBytesPerRow /= 2;
        }
        mPSStream.println(psBytesPerRow + IMAGE_STR);
        mPSStream.println("[" + destWidth + " 0 "
                          + "0 " + destHeight
                          + " " + destX + " " + destY
                          +"]concat");
        mPSStream.println(intSrcWidth + " " + intSrcHeight + " " + 8 + "["
                          + intSrcWidth + " 0 "
                          + "0 " + intSrcHeight
                          + " 0 " + 0 + "]"
                          + "/imageSrc load false 3 colorimage");
        int index = 0;
        byte[] rgbData = new byte[intSrcWidth * 3];
        try {
            index = (int) srcY * srcBitMapWidth;
            for(int i = 0; i < intSrcHeight; i++) {
                index += (int) srcX;
                index = swapBGRtoRGB(bgrData, index, rgbData);
                byte[] encodedData = rlEncode(rgbData);
                byte[] asciiData = ascii85Encode(encodedData);
                mPSStream.write(asciiData);
                mPSStream.println("");
            }
        } catch (IOException e) {
        }
        mPSStream.println(IMAGE_RESTORE);
    }
    protected void printBand(byte[] bgrData, int x, int y,
                             int width, int height)
        throws PrinterException
    {
        mPSStream.println(IMAGE_SAVE);
        int psBytesPerRow = 3 * width;
        while (psBytesPerRow > MAX_PSSTR) {
            psBytesPerRow /= 2;
        }
        mPSStream.println(psBytesPerRow + IMAGE_STR);
        mPSStream.println("[" + width + " 0 "
                          + "0 " + height
                          + " " + x + " " + y
                          +"]concat");
        mPSStream.println(width + " " + height + " " + 8 + "["
                          + width + " 0 "
                          + "0 " + -height
                          + " 0 " + height + "]"
                          + "/imageSrc load false 3 colorimage");
        int index = 0;
        byte[] rgbData = new byte[width*3];
        try {
            for(int i = 0; i < height; i++) {
                index = swapBGRtoRGB(bgrData, index, rgbData);
                byte[] encodedData = rlEncode(rgbData);
                byte[] asciiData = ascii85Encode(encodedData);
                mPSStream.write(asciiData);
                mPSStream.println("");
            }
        } catch (IOException e) {
            throw new PrinterIOException(e);
        }
        mPSStream.println(IMAGE_RESTORE);
    }
    protected Graphics2D createPathGraphics(PeekGraphics peekGraphics,
                                            PrinterJob printerJob,
                                            Printable painter,
                                            PageFormat pageFormat,
                                            int pageIndex) {
        PSPathGraphics pathGraphics;
        PeekMetrics metrics = peekGraphics.getMetrics();
        if (forcePDL == false && (forceRaster == true
                        || metrics.hasNonSolidColors()
                        || metrics.hasCompositing())) {
            pathGraphics = null;
        } else {
            BufferedImage bufferedImage = new BufferedImage(8, 8,
                                            BufferedImage.TYPE_INT_RGB);
            Graphics2D bufferedGraphics = bufferedImage.createGraphics();
            boolean canRedraw = peekGraphics.getAWTDrawingOnly() == false;
            pathGraphics =  new PSPathGraphics(bufferedGraphics, printerJob,
                                               painter, pageFormat, pageIndex,
                                               canRedraw);
        }
        return pathGraphics;
    }
    protected void selectClipPath() {
        mPSStream.println(mClipOpStr);
    }
    protected void setClip(Shape clip) {
        mLastClip = clip;
    }
    protected void setTransform(AffineTransform transform) {
        mLastTransform = transform;
    }
     protected boolean setFont(Font font) {
        mLastFont = font;
        return true;
    }
     private int[] getPSFontIndexArray(Font font, CharsetString[] charSet) {
        int[] psFont = null;
        if (mFontProps != null) {
            psFont = new int[charSet.length];
        }
        for (int i = 0; i < charSet.length && psFont != null; i++){
            CharsetString cs = charSet[i];
            CharsetEncoder fontCS = cs.fontDescriptor.encoder;
            String charsetName = cs.fontDescriptor.getFontCharsetName();
            if ("Symbol".equals(charsetName)) {
                charsetName = "symbol";
            } else if ("WingDings".equals(charsetName) ||
                       "X11Dingbats".equals(charsetName)) {
                charsetName = "dingbats";
            } else {
                charsetName = makeCharsetName(charsetName, cs.charsetChars);
            }
            int styleMask = font.getStyle() |
                FontUtilities.getFont2D(font).getStyle();
            String style = FontConfiguration.getStyleString(styleMask);
            String fontName = font.getFamily().toLowerCase(Locale.ENGLISH);
            fontName = fontName.replace(' ', '_');
            String name = mFontProps.getProperty(fontName, "");
            String psName =
                mFontProps.getProperty(name + "." + charsetName + "." + style,
                                      null);
            if (psName != null) {
                try {
                    psFont[i] =
                        Integer.parseInt(mFontProps.getProperty(psName));
                } catch(NumberFormatException e){
                    psFont = null;
                }
            } else {
                psFont = null;
            }
        }
         return psFont;
     }
    private static String escapeParens(String str) {
        if (str.indexOf('(') == -1 && str.indexOf(')') == -1 ) {
            return str;
        } else {
            int count = 0;
            int pos = 0;
            while ((pos = str.indexOf('(', pos)) != -1) {
                count++;
                pos++;
            }
            pos = 0;
            while ((pos = str.indexOf(')', pos)) != -1) {
                count++;
                pos++;
            }
            char []inArr = str.toCharArray();
            char []outArr = new char[inArr.length+count];
            pos = 0;
            for (int i=0;i<inArr.length;i++) {
                if (inArr[i] == '(' || inArr[i] == ')') {
                    outArr[pos++] = '\\';
                }
                outArr[pos++] = inArr[i];
            }
            return new String(outArr);
        }
    }
    protected int platformFontCount(Font font, String str) {
        if (mFontProps == null) {
            return 0;
        }
        CharsetString[] acs =
            ((PlatformFont)(font.getPeer())).makeMultiCharsetString(str,false);
        if (acs == null) {
            return 0;
        }
        int[] psFonts = getPSFontIndexArray(font, acs);
        return (psFonts == null) ? 0 : psFonts.length;
    }
     protected boolean textOut(Graphics g, String str, float x, float y,
                               Font mLastFont, FontRenderContext frc,
                               float width) {
        boolean didText = true;
        if (mFontProps == null) {
            return false;
        } else {
            prepDrawing();
            str = removeControlChars(str);
            if (str.length() == 0) {
                return true;
            }
            CharsetString[] acs =
                ((PlatformFont)
                 (mLastFont.getPeer())).makeMultiCharsetString(str, false);
            if (acs == null) {
                return false;
            }
            int[] psFonts = getPSFontIndexArray(mLastFont, acs);
            if (psFonts != null) {
                for (int i = 0; i < acs.length; i++){
                    CharsetString cs = acs[i];
                    CharsetEncoder fontCS = cs.fontDescriptor.encoder;
                    StringBuffer nativeStr = new StringBuffer();
                    byte[] strSeg = new byte[cs.length * 2];
                    int len = 0;
                    try {
                        ByteBuffer bb = ByteBuffer.wrap(strSeg);
                        fontCS.encode(CharBuffer.wrap(cs.charsetChars,
                                                      cs.offset,
                                                      cs.length),
                                      bb, true);
                        bb.flip();
                        len = bb.limit();
                    } catch(IllegalStateException xx){
                        continue;
                    } catch(CoderMalfunctionError xx){
                        continue;
                    }
                    float desiredWidth;
                    if (acs.length == 1 && width != 0f) {
                        desiredWidth = width;
                    } else {
                        Rectangle2D r2d =
                            mLastFont.getStringBounds(cs.charsetChars,
                                                      cs.offset,
                                                      cs.offset+cs.length,
                                                      frc);
                        desiredWidth = (float)r2d.getWidth();
                    }
                    if (desiredWidth == 0) {
                        return didText;
                    }
                    nativeStr.append('<');
                    for (int j = 0; j < len; j++){
                        byte b = strSeg[j];
                        String hexS = Integer.toHexString(b);
                        int length = hexS.length();
                        if (length > 2) {
                            hexS = hexS.substring(length - 2, length);
                        } else if (length == 1) {
                            hexS = "0" + hexS;
                        } else if (length == 0) {
                            hexS = "00";
                        }
                        nativeStr.append(hexS);
                    }
                    nativeStr.append('>');
                    getGState().emitPSFont(psFonts[i], mLastFont.getSize2D());
                    mPSStream.println(nativeStr.toString() + " " +
                                      desiredWidth + " " + x + " " + y + " " +
                                      DrawStringName);
                    x += desiredWidth;
                }
            } else {
                didText = false;
            }
        }
        return didText;
     }
    protected void setFillMode(int fillRule) {
        switch (fillRule) {
         case FILL_EVEN_ODD:
            mFillOpStr = EVEN_ODD_FILL_STR;
            mClipOpStr = EVEN_ODD_CLIP_STR;
            break;
         case FILL_WINDING:
             mFillOpStr = WINDING_FILL_STR;
             mClipOpStr = WINDING_CLIP_STR;
             break;
         default:
             throw new IllegalArgumentException();
        }
    }
    protected void setColor(Color color) {
        mLastColor = color;
    }
    protected void fillPath() {
        mPSStream.println(mFillOpStr);
    }
    protected void beginPath() {
        prepDrawing();
        mPSStream.println(NEWPATH_STR);
        mPenX = 0;
        mPenY = 0;
    }
    protected void closeSubpath() {
        mPSStream.println(CLOSEPATH_STR);
        mPenX = mStartPathX;
        mPenY = mStartPathY;
    }
    protected void moveTo(float x, float y) {
        mPSStream.println(trunc(x) + " " + trunc(y) + MOVETO_STR);
        mStartPathX = x;
        mStartPathY = y;
        mPenX = x;
        mPenY = y;
    }
    protected void lineTo(float x, float y) {
        mPSStream.println(trunc(x) + " " + trunc(y) + LINETO_STR);
        mPenX = x;
        mPenY = y;
    }
    protected void bezierTo(float control1x, float control1y,
                                float control2x, float control2y,
                                float endX, float endY) {
        mPSStream.println(trunc(control1x) + " " + trunc(control1y)
                          + " " + trunc(control2x) + " " + trunc(control2y)
                          + " " + trunc(endX) + " " + trunc(endY)
                          + CURVETO_STR);
        mPenX = endX;
        mPenY = endY;
    }
    String trunc(float f) {
        float af = Math.abs(f);
        if (af >= 1f && af <=1000f) {
            f = Math.round(f*1000)/1000f;
        }
        return Float.toString(f);
    }
    protected float getPenX() {
        return mPenX;
    }
    protected float getPenY() {
        return mPenY;
    }
    protected double getXRes() {
        return PS_XRES;
    }
    protected double getYRes() {
        return PS_YRES;
    }
    protected double getPhysicalPrintableX(Paper p) {
        return 0;
    }
    protected double getPhysicalPrintableY(Paper p) {
        return 0;
    }
    protected double getPhysicalPrintableWidth(Paper p) {
        return p.getImageableWidth();
    }
    protected double getPhysicalPrintableHeight(Paper p) {
        return p.getImageableHeight();
    }
    protected double getPhysicalPageWidth(Paper p) {
        return p.getWidth();
    }
    protected double getPhysicalPageHeight(Paper p) {
        return p.getHeight();
    }
    protected int getNoncollatedCopies() {
        return 1;
    }
    protected int getCollatedCopies() {
        return 1;
    }
    private String[] printExecCmd(String printer, String options,
                                  boolean noJobSheet,
                                  String banner, int copies, String spoolFile) {
        int PRINTER = 0x1;
        int OPTIONS = 0x2;
        int BANNER  = 0x4;
        int COPIES  = 0x8;
        int NOSHEET = 0x10;
        int pFlags = 0;
        String execCmd[];
        int ncomps = 2; 
        int n = 0;
        if (printer != null && !printer.equals("") && !printer.equals("lp")) {
            pFlags |= PRINTER;
            ncomps+=1;
        }
        if (options != null && !options.equals("")) {
            pFlags |= OPTIONS;
            ncomps+=1;
        }
        if (banner != null && !banner.equals("")) {
            pFlags |= BANNER;
            ncomps+=1;
        }
        if (copies > 1) {
            pFlags |= COPIES;
            ncomps+=1;
        }
        if (noJobSheet) {
            pFlags |= NOSHEET;
            ncomps+=1;
        }
       if (System.getProperty("os.name").equals("Linux")) {
            execCmd = new String[ncomps];
            execCmd[n++] = "/usr/bin/lpr";
            if ((pFlags & PRINTER) != 0) {
                execCmd[n++] = "-P" + printer;
            }
            if ((pFlags & BANNER) != 0) {
                execCmd[n++] = "-J"  + banner;
            }
            if ((pFlags & COPIES) != 0) {
                execCmd[n++] = "-#" + copies;
            }
            if ((pFlags & NOSHEET) != 0) {
                execCmd[n++] = "-h";
            }
            if ((pFlags & OPTIONS) != 0) {
                execCmd[n++] = new String(options);
            }
        } else {
            ncomps+=1; 
            execCmd = new String[ncomps];
            execCmd[n++] = "/usr/bin/lp";
            execCmd[n++] = "-c";           
            if ((pFlags & PRINTER) != 0) {
                execCmd[n++] = "-d" + printer;
            }
            if ((pFlags & BANNER) != 0) {
                execCmd[n++] = "-t"  + banner;
            }
            if ((pFlags & COPIES) != 0) {
                execCmd[n++] = "-n" + copies;
            }
            if ((pFlags & NOSHEET) != 0) {
                execCmd[n++] = "-o nobanner";
            }
            if ((pFlags & OPTIONS) != 0) {
                execCmd[n++] = "-o" + options;
            }
        }
        execCmd[n++] = spoolFile;
        return execCmd;
    }
    private static int swapBGRtoRGB(byte[] image, int index, byte[] dest) {
        int destIndex = 0;
        while(index < image.length-2 && destIndex < dest.length-2) {
            dest[destIndex++] = image[index+2];
            dest[destIndex++] = image[index+1];
            dest[destIndex++] = image[index+0];
            index+=3;
        }
        return index;
    }
    private String makeCharsetName(String name, char[] chs) {
        if (name.equals("Cp1252") || name.equals("ISO8859_1")) {
            return "latin1";
        } else if (name.equals("UTF8")) {
            for (int i=0; i < chs.length; i++) {
                if (chs[i] > 255) {
                    return name.toLowerCase();
                }
            }
            return "latin1";
        } else if (name.startsWith("ISO8859")) {
            for (int i=0; i < chs.length; i++) {
                if (chs[i] > 127) {
                    return name.toLowerCase();
                }
            }
            return "latin1";
        } else {
            return name.toLowerCase();
        }
    }
    private void prepDrawing() {
        while (isOuterGState() == false
               && (getGState().canSetClip(mLastClip) == false
                   || getGState().mTransform.equals(mLastTransform) == false)) {
            grestore();
        }
        getGState().emitPSColor(mLastColor);
        if (isOuterGState()) {
            gsave();
            getGState().emitTransform(mLastTransform);
            getGState().emitPSClip(mLastClip);
        }
    }
    private GState getGState() {
        int count = mGStateStack.size();
        return (GState) mGStateStack.get(count - 1);
    }
    private void gsave() {
        GState oldGState = getGState();
        mGStateStack.add(new GState(oldGState));
        mPSStream.println(GSAVE_STR);
    }
    private void grestore() {
        int count = mGStateStack.size();
        mGStateStack.remove(count - 1);
        mPSStream.println(GRESTORE_STR);
    }
    private boolean isOuterGState() {
        return mGStateStack.size() == 1;
    }
    private class GState{
        Color mColor;
        Shape mClip;
        Font mFont;
        AffineTransform mTransform;
        GState() {
            mColor = Color.black;
            mClip = null;
            mFont = null;
            mTransform = new AffineTransform();
        }
        GState(GState copyGState) {
            mColor = copyGState.mColor;
            mClip = copyGState.mClip;
            mFont = copyGState.mFont;
            mTransform = copyGState.mTransform;
        }
        boolean canSetClip(Shape clip) {
            return mClip == null || mClip.equals(clip);
        }
        void emitPSClip(Shape clip) {
            if (clip != null
                && (mClip == null || mClip.equals(clip) == false)) {
                String saveFillOp = mFillOpStr;
                String saveClipOp = mClipOpStr;
                convertToPSPath(clip.getPathIterator(new AffineTransform()));
                selectClipPath();
                mClip = clip;
                mClipOpStr = saveFillOp;
                mFillOpStr = saveFillOp;
            }
        }
        void emitTransform(AffineTransform transform) {
            if (transform != null && transform.equals(mTransform) == false) {
                double[] matrix = new double[6];
                transform.getMatrix(matrix);
                mPSStream.println("[" + (float)matrix[0]
                                  + " " + (float)matrix[1]
                                  + " " + (float)matrix[2]
                                  + " " + (float)matrix[3]
                                  + " " + (float)matrix[4]
                                  + " " + (float)matrix[5]
                                  + "] concat");
                mTransform = transform;
            }
        }
        void emitPSColor(Color color) {
            if (color != null && color.equals(mColor) == false) {
                float[] rgb = color.getRGBColorComponents(null);
                if (rgb[0] == rgb[1] && rgb[1] == rgb[2]) {
                    mPSStream.println(rgb[0] + SETGRAY_STR);
                } else {
                    mPSStream.println(rgb[0] + " "
                                      + rgb[1] + " "
                                      + rgb[2] + " "
                                      + SETRGBCOLOR_STR);
                }
                mColor = color;
            }
        }
        void emitPSFont(int psFontIndex, float fontSize) {
            mPSStream.println(fontSize + " " +
                              psFontIndex + " " + SetFontName);
        }
    }
        void convertToPSPath(PathIterator pathIter) {
            float[] segment = new float[6];
            int segmentType;
            int fillRule;
            if (pathIter.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
                fillRule = FILL_EVEN_ODD;
            } else {
                fillRule = FILL_WINDING;
            }
            beginPath();
            setFillMode(fillRule);
            while (pathIter.isDone() == false) {
                segmentType = pathIter.currentSegment(segment);
                switch (segmentType) {
                 case PathIterator.SEG_MOVETO:
                    moveTo(segment[0], segment[1]);
                    break;
                 case PathIterator.SEG_LINETO:
                    lineTo(segment[0], segment[1]);
                    break;
                 case PathIterator.SEG_QUADTO:
                    float lastX = getPenX();
                    float lastY = getPenY();
                    float c1x = lastX + (segment[0] - lastX) * 2 / 3;
                    float c1y = lastY + (segment[1] - lastY) * 2 / 3;
                    float c2x = segment[2] - (segment[2] - segment[0]) * 2/ 3;
                    float c2y = segment[3] - (segment[3] - segment[1]) * 2/ 3;
                    bezierTo(c1x, c1y,
                             c2x, c2y,
                             segment[2], segment[3]);
                    break;
                 case PathIterator.SEG_CUBICTO:
                    bezierTo(segment[0], segment[1],
                             segment[2], segment[3],
                             segment[4], segment[5]);
                    break;
                 case PathIterator.SEG_CLOSE:
                    closeSubpath();
                    break;
                }
                pathIter.next();
            }
        }
    protected void deviceFill(PathIterator pathIter, Color color,
                              AffineTransform tx, Shape clip) {
        setTransform(tx);
        setClip(clip);
        setColor(color);
        convertToPSPath(pathIter);
        mPSStream.println(GSAVE_STR);
        selectClipPath();
        fillPath();
        mPSStream.println(GRESTORE_STR + " " + NEWPATH_STR);
    }
     private byte[] rlEncode(byte[] inArr) {
         int inIndex = 0;
         int outIndex = 0;
         int startIndex = 0;
         int runLen = 0;
         byte[] outArr = new byte[(inArr.length * 2) +2];
         while (inIndex < inArr.length) {
             if (runLen == 0) {
                 startIndex = inIndex++;
                 runLen=1;
             }
             while (runLen < 128 && inIndex < inArr.length &&
                    inArr[inIndex] == inArr[startIndex]) {
                 runLen++; 
                 inIndex++;
             }
             if (runLen > 1) {
                 outArr[outIndex++] = (byte)(257 - runLen);
                 outArr[outIndex++] = inArr[startIndex];
                 runLen = 0;
                 continue; 
             }
             while (runLen < 128 && inIndex < inArr.length &&
                    inArr[inIndex] != inArr[inIndex-1]) {
                 runLen++; 
                 inIndex++;
             }
             outArr[outIndex++] = (byte)(runLen - 1);
             for (int i = startIndex; i < startIndex+runLen; i++) {
                 outArr[outIndex++] = inArr[i];
             }
             runLen = 0;
         }
         outArr[outIndex++] = (byte)128;
         byte[] encodedData = new byte[outIndex];
         System.arraycopy(outArr, 0, encodedData, 0, outIndex);
         return encodedData;
     }
    private byte[] ascii85Encode(byte[] inArr) {
        byte[]  outArr = new byte[((inArr.length+4) * 5 / 4) + 2];
        long p1 = 85;
        long p2 = p1*p1;
        long p3 = p1*p2;
        long p4 = p1*p3;
        byte pling = '!';
        int i = 0;
        int olen = 0;
        long val, rem;
        while (i+3 < inArr.length) {
            val = ((long)((inArr[i++]&0xff))<<24) +
                  ((long)((inArr[i++]&0xff))<<16) +
                  ((long)((inArr[i++]&0xff))<< 8) +
                  ((long)(inArr[i++]&0xff));
            if (val == 0) {
                outArr[olen++] = 'z';
            } else {
                rem = val;
                outArr[olen++] = (byte)(rem / p4 + pling); rem = rem % p4;
                outArr[olen++] = (byte)(rem / p3 + pling); rem = rem % p3;
                outArr[olen++] = (byte)(rem / p2 + pling); rem = rem % p2;
                outArr[olen++] = (byte)(rem / p1 + pling); rem = rem % p1;
                outArr[olen++] = (byte)(rem + pling);
            }
        }
        if (i < inArr.length) {
            int n = inArr.length - i; 
            val = 0;
            while (i < inArr.length) {
                val = (val << 8) + (inArr[i++]&0xff);
            }
            int append = 4 - n;
            while (append-- > 0) {
                val = val << 8;
            }
            byte []c = new byte[5];
            rem = val;
            c[0] = (byte)(rem / p4 + pling); rem = rem % p4;
            c[1] = (byte)(rem / p3 + pling); rem = rem % p3;
            c[2] = (byte)(rem / p2 + pling); rem = rem % p2;
            c[3] = (byte)(rem / p1 + pling); rem = rem % p1;
            c[4] = (byte)(rem + pling);
            for (int b = 0; b < n+1 ; b++) {
                outArr[olen++] = c[b];
            }
        }
        outArr[olen++]='~'; outArr[olen++]='>';
        byte[] retArr = new byte[olen];
        System.arraycopy(outArr, 0, retArr, 0, olen);
        return retArr;
    }
    public static class PluginPrinter implements Printable {
        private EPSPrinter epsPrinter;
        private Component applet;
        private PrintStream stream;
        private String epsTitle;
        private int bx, by, bw, bh;
        private int width, height;
        public PluginPrinter(Component applet,
                             PrintStream stream,
                             int x, int y, int w, int h) {
            this.applet = applet;
            this.epsTitle = "Java Plugin Applet";
            this.stream = stream;
            bx = x;
            by = y;
            bw = w;
            bh = h;
            width = applet.size().width;
            height = applet.size().height;
            epsPrinter = new EPSPrinter(this, epsTitle, stream,
                                        0, 0, width, height);
        }
        public void printPluginPSHeader() {
            stream.println("%%BeginDocument: JavaPluginApplet");
        }
        public void printPluginApplet() {
            try {
                epsPrinter.print();
            } catch (PrinterException e) {
            }
        }
        public void printPluginPSTrailer() {
            stream.println("%%EndDocument: JavaPluginApplet");
            stream.flush();
        }
        public void printAll() {
            printPluginPSHeader();
            printPluginApplet();
            printPluginPSTrailer();
        }
        public int print(Graphics g, PageFormat pf, int pgIndex) {
            if (pgIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            } else {
                applet.printAll(g);
                return Printable.PAGE_EXISTS;
            }
        }
    }
    public static class EPSPrinter implements Pageable {
        private PageFormat pf;
        private PSPrinterJob job;
        private int llx, lly, urx, ury;
        private Printable printable;
        private PrintStream stream;
        private String epsTitle;
        public EPSPrinter(Printable printable, String title,
                          PrintStream stream,
                          int x, int y, int wid, int hgt) {
            this.printable = printable;
            this.epsTitle = title;
            this.stream = stream;
            llx = x;
            lly = y;
            urx = llx+wid;
            ury = lly+hgt;
            Paper p = new Paper();
            p.setSize((double)wid, (double)hgt);
            p.setImageableArea(0.0,0.0, (double)wid, (double)hgt);
            pf = new PageFormat();
            pf.setPaper(p);
        }
        public void print() throws PrinterException {
            stream.println("%!PS-Adobe-3.0 EPSF-3.0");
            stream.println("%%BoundingBox: " +
                           llx + " " + lly + " " + urx + " " + ury);
            stream.println("%%Title: " + epsTitle);
            stream.println("%%Creator: Java Printing");
            stream.println("%%CreationDate: " + new java.util.Date());
            stream.println("%%EndComments");
            stream.println("/pluginSave save def");
            stream.println("mark"); 
            job = new PSPrinterJob();
            job.epsPrinter = this; 
            job.mPSStream = stream;
            job.mDestType = RasterPrinterJob.STREAM; 
            job.startDoc();
            try {
                job.printPage(this, 0);
            } catch (Throwable t) {
                if (t instanceof PrinterException) {
                    throw (PrinterException)t;
                } else {
                    throw new PrinterException(t.toString());
                }
            } finally {
                stream.println("cleartomark"); 
                stream.println("pluginSave restore");
                job.endDoc();
            }
            stream.flush();
        }
        public int getNumberOfPages() {
            return 1;
        }
        public PageFormat getPageFormat(int pgIndex) {
            if (pgIndex > 0) {
                throw new IndexOutOfBoundsException("pgIndex");
            } else {
                return pf;
            }
        }
        public Printable getPrintable(int pgIndex) {
            if (pgIndex > 0) {
                throw new IndexOutOfBoundsException("pgIndex");
            } else {
            return printable;
            }
        }
    }
}
