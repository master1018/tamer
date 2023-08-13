public class JpegDecoder extends ImageDecoder {
    public static final int JCS_GRAYSCALE = 1;
    public static final int JCS_RGB = 2;
    private static final int hintflagsProgressive =
            ImageConsumer.SINGLEFRAME | 
            ImageConsumer.TOPDOWNLEFTRIGHT | 
            ImageConsumer.COMPLETESCANLINES; 
    private static final int hintflagsSingle =
            ImageConsumer.SINGLEPASS |
            hintflagsProgressive;
    private static final int BUFFER_SIZE = 1024;
    private byte buffer[] = new byte[BUFFER_SIZE];
    private static ColorModel cmRGB;
    private static ColorModel cmGray;
    private static native void initIDs();
    private long hNativeDecoder = 0; 
    private boolean headerDone = false;
    private int imageWidth = -1;
    private int imageHeight = -1;
    private boolean progressive = false;
    private int jpegColorSpace = 0;
    private int bytesConsumed = 0;
    private int currScanline = 0;
    private ColorModel cm = null;
    static {
        System.loadLibrary("jpegdecoder"); 
        cmGray = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE
        );
        cmRGB = new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
        initIDs();
    }
    public JpegDecoder(DecodingImageSource src, InputStream is) {
        super(src, is);
    }
    private native Object decode(
            byte[] input,
            int bytesInBuffer,
            long hDecoder);
    private static native void releaseNativeDecoder(long hDecoder);
    @Override
    public void decodeImage() throws IOException {
        try {
            int bytesRead = 0, dataLength = 0;
            boolean eosReached = false;
            int needBytes, offset, bytesInBuffer = 0;
            byte byteOut[] = null;
            int intOut[] = null;
            for (;;) {
                needBytes = BUFFER_SIZE - bytesInBuffer;
                offset = bytesInBuffer;
                bytesRead = inputStream.read(buffer, offset, needBytes);
                if (bytesRead < 0) {
                    bytesRead = 0;
                    eosReached = true;
                } 
                bytesInBuffer += bytesRead;
                Object arr = decode(
                        buffer,
                        bytesInBuffer,
                        hNativeDecoder);
                bytesInBuffer -= bytesConsumed;
                if (!headerDone && imageWidth != -1) {
                    returnHeader();
                    headerDone = true;
                }
                if (bytesConsumed < 0) {
                    break; 
                }
                if (arr instanceof byte[]) {
                    byteOut = (byte[]) arr;
                    dataLength = byteOut.length;
                    returnData(byteOut, currScanline);
                } else if (arr instanceof int[]) {
                    intOut = (int[]) arr;
                    dataLength = intOut.length;
                    returnData(intOut, currScanline);
                } else {
                    dataLength = 0;
                }
                if (hNativeDecoder == 0) {
                    break;
                }
                if (dataLength == 0 && eosReached) {
                    releaseNativeDecoder(hNativeDecoder);
                    break; 
                }
            }
            imageComplete(ImageConsumer.STATICIMAGEDONE);
        } catch (IOException e) {
            throw e;
        } finally {
            closeStream();
        }
    }
    public void returnHeader() {
        setDimensions(imageWidth, imageHeight);
        switch (jpegColorSpace) {
            case JCS_GRAYSCALE: cm = cmGray; break;
            case JCS_RGB: cm = cmRGB; break;
            default: 
                throw new IllegalArgumentException(Messages.getString("awt.3D")); 
        }
        setColorModel(cm);
        setHints(progressive ? hintflagsProgressive : hintflagsSingle);
        setProperties(new Hashtable<Object, Object>()); 
    }
    public void returnData(int data[], int currScanLine) {
        int numScanlines = data.length / imageWidth;
        if (numScanlines > 0) {
            setPixels(
                    0, currScanLine - numScanlines,
                    imageWidth, numScanlines,
                    cm, data, 0, imageWidth
            );
        }
    }
    public void returnData(byte data[], int currScanLine) {
        int numScanlines = data.length / imageWidth;
        if (numScanlines > 0) {
            setPixels(
                    0, currScanLine - numScanlines,
                    imageWidth, numScanlines,
                    cm, data, 0, imageWidth
            );
        }
    }
}
