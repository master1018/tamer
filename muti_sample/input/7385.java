public class WDataTransferer extends DataTransferer {
    private static final String[] predefinedClipboardNames = {
        "",
        "TEXT",
        "BITMAP",
        "METAFILEPICT",
        "SYLK",
        "DIF",
        "TIFF",
        "OEM TEXT",
        "DIB",
        "PALETTE",
        "PENDATA",
        "RIFF",
        "WAVE",
        "UNICODE TEXT",
        "ENHMETAFILE",
        "HDROP",
        "LOCALE",
        "DIBV5"
    };
    private static final Map predefinedClipboardNameMap;
    static {
        Map tempMap = new HashMap(predefinedClipboardNames.length, 1.0f);
        for (int i = 1; i < predefinedClipboardNames.length; i++) {
            tempMap.put(predefinedClipboardNames[i], Long.valueOf(i));
        }
        predefinedClipboardNameMap = Collections.synchronizedMap(tempMap);
    }
    public static final int CF_TEXT = 1;
    public static final int CF_METAFILEPICT = 3;
    public static final int CF_DIB = 8;
    public static final int CF_ENHMETAFILE = 14;
    public static final int CF_HDROP = 15;
    public static final int CF_LOCALE = 16;
    public static final long CF_HTML = registerClipboardFormat("HTML Format");
    public static final long CFSTR_INETURL = registerClipboardFormat("UniformResourceLocator");
    public static final long CF_PNG = registerClipboardFormat("PNG");
    public static final long CF_JFIF = registerClipboardFormat("JFIF");
    public static final long CF_FILEGROUPDESCRIPTORW = registerClipboardFormat("FileGroupDescriptorW");
    public static final long CF_FILEGROUPDESCRIPTORA = registerClipboardFormat("FileGroupDescriptor");
    private static final Long L_CF_LOCALE = (Long)
      predefinedClipboardNameMap.get(predefinedClipboardNames[CF_LOCALE]);
    private static final DirectColorModel directColorModel =
        new DirectColorModel(24,
                             0x00FF0000,  
                             0x0000FF00,  
                             0x000000FF); 
    private static final int[] bandmasks = new int[] {
        directColorModel.getRedMask(),
        directColorModel.getGreenMask(),
        directColorModel.getBlueMask() };
    private WDataTransferer() {
    }
    private static WDataTransferer transferer;
    public static WDataTransferer getInstanceImpl() {
        if (transferer == null) {
            synchronized (WDataTransferer.class) {
                if (transferer == null) {
                    transferer = new WDataTransferer();
                }
            }
        }
        return transferer;
    }
    public SortedMap getFormatsForFlavors(DataFlavor[] flavors, FlavorTable map) {
        SortedMap retval = super.getFormatsForFlavors(flavors, map);
        retval.remove(L_CF_LOCALE);
        return retval;
    }
    public String getDefaultUnicodeEncoding() {
        return "utf-16le";
    }
    public byte[] translateTransferable(Transferable contents,
                                        DataFlavor flavor,
                                        long format) throws IOException
    {
        byte[] bytes = super.translateTransferable(contents, flavor, format);
        if (format == CF_HTML) {
            bytes = HTMLCodec.convertToHTMLFormat(bytes);
        }
        return bytes;
    }
    protected Object translateBytesOrStream(InputStream str, byte[] bytes,
                                            DataFlavor flavor, long format,
                                            Transferable localeTransferable)
        throws IOException
    {
        if (format == CF_HTML && flavor.isFlavorTextType()) {
            if (str == null) {
                str = new ByteArrayInputStream(bytes);
                bytes = null;
            }
            str = new HTMLCodec(str,  EHTMLReadMode.HTML_READ_SELECTION);
        }
        if (format == CF_FILEGROUPDESCRIPTORA || format == CF_FILEGROUPDESCRIPTORW) {
            if (null != str ) {
                str.close();
            }
            if (bytes == null || !DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            String st = new String(bytes, 0, bytes.length, "UTF-16LE");
            String[] filenames = st.split("\0");
            if( 0 == filenames.length ){
                return null;
            }
            File[] files = new File[filenames.length];
            for (int i = 0; i < filenames.length; ++i) {
                files[i] = new File(filenames[i]);
                files[i].deleteOnExit();
            }
            return Arrays.asList(files);
        }
        if (format == CFSTR_INETURL &&
            URL.class.equals(flavor.getRepresentationClass()))
        {
            if (bytes == null) {
                bytes = inputStreamToByteArray(str);
                str = null;
            }
            String charset = getDefaultTextCharset();
            if (localeTransferable != null && localeTransferable.
                isDataFlavorSupported(javaTextEncodingFlavor))
            {
                try {
                    charset = new String((byte[])localeTransferable.
                                   getTransferData(javaTextEncodingFlavor),
                                   "UTF-8");
                } catch (UnsupportedFlavorException cannotHappen) {
                }
            }
            return new URL(new String(bytes, charset));
        }
        return super.translateBytesOrStream(str, bytes, flavor, format,
                                            localeTransferable);
    }
    public boolean isLocaleDependentTextFormat(long format) {
        return format == CF_TEXT || format == CFSTR_INETURL;
    }
    public boolean isFileFormat(long format) {
        return format == CF_HDROP || format == CF_FILEGROUPDESCRIPTORA || format == CF_FILEGROUPDESCRIPTORW;
    }
    protected Long getFormatForNativeAsLong(String str) {
        Long format = (Long)predefinedClipboardNameMap.get(str);
        if (format == null) {
            format = Long.valueOf(registerClipboardFormat(str));
        }
        return format;
    }
    protected String getNativeForFormat(long format) {
        return (format < predefinedClipboardNames.length)
            ? predefinedClipboardNames[(int)format]
            : getClipboardFormatName(format);
    }
    private final ToolkitThreadBlockedHandler handler =
        new WToolkitThreadBlockedHandler();
    public ToolkitThreadBlockedHandler getToolkitThreadBlockedHandler() {
        return handler;
    }
    private static native long registerClipboardFormat(String str);
    private static native String getClipboardFormatName(long format);
    public boolean isImageFormat(long format) {
        return format == CF_DIB || format == CF_ENHMETAFILE ||
               format == CF_METAFILEPICT || format == CF_PNG ||
               format == CF_JFIF;
    }
    protected byte[] imageToPlatformBytes(Image image, long format)
      throws IOException {
        String mimeType = null;
        if (format == CF_PNG) {
            mimeType = "image/png";
        } else if (format == CF_JFIF) {
            mimeType = "image/jpeg";
        }
        if (mimeType != null) {
            return imageToStandardBytes(image, mimeType);
        }
        int width = 0;
        int height = 0;
        if (image instanceof ToolkitImage) {
            ImageRepresentation ir = ((ToolkitImage)image).getImageRep();
            ir.reconstruct(ImageObserver.ALLBITS);
            width = ir.getWidth();
            height = ir.getHeight();
        } else {
            width = image.getWidth(null);
            height = image.getHeight(null);
        }
        int mod = (width * 3) % 4;
        int pad = mod > 0 ? 4 - mod : 0;
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8};
        int[] bOffs = {2, 1, 0};
        ColorModel colorModel =
            new ComponentColorModel(cs, nBits, false, false,
                                    Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster =
            Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height,
                                           width * 3 + pad, 3, bOffs, null);
        BufferedImage bimage = new BufferedImage(colorModel, raster, false, null);
        AffineTransform imageFlipTransform =
            new AffineTransform(1, 0, 0, -1, 0, height);
        Graphics2D g2d = bimage.createGraphics();
        try {
            g2d.drawImage(image, imageFlipTransform, null);
        } finally {
            g2d.dispose();
        }
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        byte[] imageData = buffer.getData();
        return imageDataToPlatformImageBytes(imageData, width, height, format);
    }
    private static final byte [] UNICODE_NULL_TERMINATOR =  new byte [] {0,0};
    protected ByteArrayOutputStream convertFileListToBytes(ArrayList<String> fileList)
        throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if(fileList.isEmpty()) {
            bos.write(UNICODE_NULL_TERMINATOR);
        } else {
            for (int i = 0; i < fileList.size(); i++) {
                byte[] bytes = fileList.get(i).getBytes(getDefaultUnicodeEncoding());
                bos.write(bytes, 0, bytes.length);
                bos.write(UNICODE_NULL_TERMINATOR);
            }
        }
        bos.write(UNICODE_NULL_TERMINATOR);
        return bos;
    }
    private native byte[] imageDataToPlatformImageBytes(byte[] imageData,
                                                        int width, int height,
                                                        long format);
    protected Image platformImageBytesOrStreamToImage(InputStream str,
                                                      byte[] bytes,
                                                      long format)
      throws IOException {
        String mimeType = null;
        if (format == CF_PNG) {
            mimeType = "image/png";
        } else if (format == CF_JFIF) {
            mimeType = "image/jpeg";
        }
        if (mimeType != null) {
            return standardImageBytesOrStreamToImage(str, bytes, mimeType);
        }
        if (bytes == null) {
            bytes = inputStreamToByteArray(str);
        }
        int[] imageData = platformImageBytesToImageData(bytes, format);
        if (imageData == null) {
            throw new IOException("data translation failed");
        }
        int len = imageData.length - 2;
        int width = imageData[len];
        int height = imageData[len + 1];
        DataBufferInt buffer = new DataBufferInt(imageData, len);
        WritableRaster raster = Raster.createPackedRaster(buffer, width,
                                                          height, width,
                                                          bandmasks, null);
        return new BufferedImage(directColorModel, raster, false, null);
    }
    private native int[] platformImageBytesToImageData(byte[] bytes,
                                                       long format)
      throws IOException;
    protected native String[] dragQueryFile(byte[] bytes);
}
final class WToolkitThreadBlockedHandler extends Mutex
                       implements ToolkitThreadBlockedHandler {
    public void enter() {
        if (!isOwned()) {
            throw new IllegalMonitorStateException();
        }
        unlock();
        startSecondaryEventLoop();
        lock();
    }
    public void exit() {
        if (!isOwned()) {
            throw new IllegalMonitorStateException();
        }
        WToolkit.quitSecondaryEventLoop();
    }
    private native void startSecondaryEventLoop();
}
enum EHTMLReadMode {
    HTML_READ_ALL,
    HTML_READ_FRAGMENT,
    HTML_READ_SELECTION
}
class HTMLCodec extends InputStream {
    public static final String ENCODING = "UTF-8";
    public static final String VERSION = "Version:";
    public static final String START_HTML = "StartHTML:";
    public static final String END_HTML = "EndHTML:";
    public static final String START_FRAGMENT = "StartFragment:";
    public static final String END_FRAGMENT = "EndFragment:";
    public static final String START_SELECTION = "StartSelection:"; 
    public static final String END_SELECTION = "EndSelection:"; 
    public static final String START_FRAGMENT_CMT = "<!--StartFragment-->";
    public static final String END_FRAGMENT_CMT = "<!--EndFragment-->";
    public static final String SOURCE_URL = "SourceURL:";
    public static final String DEF_SOURCE_URL = "about:blank";
    public static final String EOLN = "\r\n";
    private static final String VERSION_NUM = "1.0";
    private static final int PADDED_WIDTH = 10;
    private static String toPaddedString(int n, int width) {
        String string = "" + n;
        int len = string.length();
        if (n >= 0 && len < width) {
            char[] array = new char[width - len];
            Arrays.fill(array, '0');
            StringBuffer buffer = new StringBuffer(width);
            buffer.append(array);
            buffer.append(string);
            string = buffer.toString();
        }
        return string;
    }
    public static byte[] convertToHTMLFormat(byte[] bytes) {
        String htmlPrefix = "";
        String htmlSuffix = "";
        {
            String stContext = new String(bytes);
            String stUpContext = stContext.toUpperCase();
            if( -1 == stUpContext.indexOf("<HTML") ) {
                htmlPrefix = "<HTML>";
                htmlSuffix = "</HTML>";
                if( -1 == stUpContext.indexOf("<BODY") ) {
                    htmlPrefix = htmlPrefix +"<BODY>";
                    htmlSuffix = "</BODY>" + htmlSuffix;
                };
            };
            htmlPrefix = htmlPrefix + START_FRAGMENT_CMT;
            htmlSuffix = END_FRAGMENT_CMT + htmlSuffix;
        }
        String stBaseUrl = DEF_SOURCE_URL;
        int nStartHTML =
            VERSION.length() + VERSION_NUM.length() + EOLN.length()
            + START_HTML.length() + PADDED_WIDTH + EOLN.length()
            + END_HTML.length() + PADDED_WIDTH + EOLN.length()
            + START_FRAGMENT.length() + PADDED_WIDTH + EOLN.length()
            + END_FRAGMENT.length() + PADDED_WIDTH + EOLN.length()
            + SOURCE_URL.length() + stBaseUrl.length() + EOLN.length()
            ;
        int nStartFragment = nStartHTML + htmlPrefix.length();
        int nEndFragment = nStartFragment + bytes.length - 1;
        int nEndHTML = nEndFragment + htmlSuffix.length();
        StringBuilder header = new StringBuilder(
            nStartFragment
            + START_FRAGMENT_CMT.length()
        );
        header.append(VERSION);
        header.append(VERSION_NUM);
        header.append(EOLN);
        header.append(START_HTML);
        header.append(toPaddedString(nStartHTML, PADDED_WIDTH));
        header.append(EOLN);
        header.append(END_HTML);
        header.append(toPaddedString(nEndHTML, PADDED_WIDTH));
        header.append(EOLN);
        header.append(START_FRAGMENT);
        header.append(toPaddedString(nStartFragment, PADDED_WIDTH));
        header.append(EOLN);
        header.append(END_FRAGMENT);
        header.append(toPaddedString(nEndFragment, PADDED_WIDTH));
        header.append(EOLN);
        header.append(SOURCE_URL);
        header.append(stBaseUrl);
        header.append(EOLN);
        header.append(htmlPrefix);
        byte[] headerBytes = null, trailerBytes = null;
        try {
            headerBytes = header.toString().getBytes(ENCODING);
            trailerBytes = htmlSuffix.getBytes(ENCODING);
        } catch (UnsupportedEncodingException cannotHappen) {
        }
        byte[] retval = new byte[headerBytes.length + bytes.length +
                                 trailerBytes.length];
        System.arraycopy(headerBytes, 0, retval, 0, headerBytes.length);
        System.arraycopy(bytes, 0, retval, headerBytes.length,
                       bytes.length - 1);
        System.arraycopy(trailerBytes, 0, retval,
                         headerBytes.length + bytes.length - 1,
                         trailerBytes.length);
        retval[retval.length-1] = 0;
        return retval;
    }
    private final BufferedInputStream bufferedStream;
    private boolean descriptionParsed = false;
    private boolean closed = false;
    public static final int BYTE_BUFFER_LEN = 8192;
    public static final int CHAR_BUFFER_LEN = BYTE_BUFFER_LEN / 3;
    private static final String FAILURE_MSG =
        "Unable to parse HTML description: ";
    private static final String INVALID_MSG =
        " invalid";
    private long   iHTMLStart,
                   iHTMLEnd,  
                   iFragStart,
                   iFragEnd,  
                   iSelStart, 
                   iSelEnd;   
    private String stBaseURL; 
    private String stVersion; 
    private long iStartOffset,
                 iEndOffset,
                 iReadCount;
    private EHTMLReadMode readMode;
    public HTMLCodec(
        InputStream _bytestream,
        EHTMLReadMode _readMode) throws IOException
    {
        bufferedStream = new BufferedInputStream(_bytestream, BYTE_BUFFER_LEN);
        readMode = _readMode;
    }
    public synchronized String getBaseURL() throws IOException
    {
        if( !descriptionParsed ) {
            parseDescription();
        }
        return stBaseURL;
    }
    public synchronized String getVersion() throws IOException
    {
        if( !descriptionParsed ) {
            parseDescription();
        }
        return stVersion;
    }
    private void parseDescription() throws IOException
    {
        stBaseURL = null;
        stVersion = null;
        iHTMLEnd =
            iHTMLStart =
                iFragEnd =
                    iFragStart =
                        iSelEnd =
                            iSelStart = -1;
        bufferedStream.mark(BYTE_BUFFER_LEN);
        String astEntries[] = new String[] {
            VERSION,
            START_HTML,
            END_HTML,
            START_FRAGMENT,
            END_FRAGMENT,
            START_SELECTION,
            END_SELECTION,
            SOURCE_URL
        };
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(
                bufferedStream,
                ENCODING
            ),
            CHAR_BUFFER_LEN
        );
        long iHeadSize = 0;
        long iCRSize = EOLN.length();
        int iEntCount = astEntries.length;
        boolean bContinue = true;
        for( int  iEntry = 0; iEntry < iEntCount; ++iEntry ){
            String stLine = bufferedReader.readLine();
            if( null==stLine ) {
                break;
            }
            for( ; iEntry < iEntCount; ++iEntry ){
                if( !stLine.startsWith(astEntries[iEntry]) ) {
                    continue;
                }
                iHeadSize += stLine.length() + iCRSize;
                String stValue = stLine.substring(astEntries[iEntry].length()).trim();
                if( null!=stValue ) {
                    try{
                        switch( iEntry ){
                        case 0:
                            stVersion = stValue;
                            break;
                        case 1:
                            iHTMLStart = Integer.parseInt(stValue);
                            break;
                        case 2:
                            iHTMLEnd = Integer.parseInt(stValue);
                            break;
                        case 3:
                            iFragStart = Integer.parseInt(stValue);
                            break;
                        case 4:
                            iFragEnd = Integer.parseInt(stValue);
                            break;
                        case 5:
                            iSelStart = Integer.parseInt(stValue);
                            break;
                        case 6:
                            iSelEnd = Integer.parseInt(stValue);
                            break;
                        case 7:
                            stBaseURL = stValue;
                            break;
                        };
                    } catch ( NumberFormatException e ) {
                        throw new IOException(FAILURE_MSG + astEntries[iEntry]+ " value " + e + INVALID_MSG);
                    }
                }
                break;
            }
        }
        if( -1 == iHTMLStart )
            iHTMLStart = iHeadSize;
        if( -1 == iFragStart )
            iFragStart = iHTMLStart;
        if( -1 == iFragEnd )
            iFragEnd = iHTMLEnd;
        if( -1 == iSelStart )
            iSelStart = iFragStart;
        if( -1 == iSelEnd )
            iSelEnd = iFragEnd;
        switch( readMode ){
        case HTML_READ_ALL:
            iStartOffset = iHTMLStart;
            iEndOffset = iHTMLEnd;
            break;
        case HTML_READ_FRAGMENT:
            iStartOffset = iFragStart;
            iEndOffset = iFragEnd;
            break;
        case HTML_READ_SELECTION:
        default:
            iStartOffset = iSelStart;
            iEndOffset = iSelEnd;
            break;
        }
        bufferedStream.reset();
        if( -1 == iStartOffset ){
            throw new IOException(FAILURE_MSG + "invalid HTML format.");
        }
        int curOffset = 0;
        while (curOffset < iStartOffset){
            curOffset += bufferedStream.skip(iStartOffset - curOffset);
        }
        iReadCount = curOffset;
        if( iStartOffset != iReadCount ){
            throw new IOException(FAILURE_MSG + "Byte stream ends in description.");
        }
        descriptionParsed = true;
    }
    public synchronized int read() throws IOException {
        if( closed ){
            throw new IOException("Stream closed");
        }
        if( !descriptionParsed ){
            parseDescription();
        }
        if( -1 != iEndOffset && iReadCount >= iEndOffset ) {
            return -1;
        }
        int retval = bufferedStream.read();
        if( retval == -1 ) {
            return -1;
        }
        ++iReadCount;
        return retval;
    }
    public synchronized void close() throws IOException {
        if( !closed ){
            closed = true;
            bufferedStream.close();
        }
    }
}
