public class PngDecoder extends ImageDecoder {
    private static native void initIDs();
    static {
        System.loadLibrary("gl"); 
        initIDs();
    }
    private static final int hintflags =
            ImageConsumer.SINGLEFRAME | 
            ImageConsumer.TOPDOWNLEFTRIGHT | 
            ImageConsumer.COMPLETESCANLINES; 
    private static final int PNG_COLOR_TYPE_GRAY = 0;
    private static final int PNG_COLOR_TYPE_RGB = 2;
    private static final int PNG_COLOR_TYPE_PLTE = 3;
    private static final int PNG_COLOR_TYPE_GRAY_ALPHA = 4;
    private static final int PNG_COLOR_TYPE_RGBA = 6;
    private static final int INPUT_BUFFER_SIZE = 4096;
    private byte buffer[] = new byte[INPUT_BUFFER_SIZE];
    byte byteOut[];
    int intOut[];
    private long hNativeDecoder;
    int imageWidth, imageHeight;
    int colorType;
    int bitDepth;
    byte cmap[];
    boolean transferInts; 
    int dataElementsPerPixel = 1;
    ColorModel cm;
    int updateFromScanline; 
    int numScanlines; 
    private native long decode(byte[] input, int bytesInBuffer, long hDecoder);
    private static native void releaseNativeDecoder(long hDecoder);
    public PngDecoder(DecodingImageSource src, InputStream is) {
        super(src, is);
    }
    @Override
    public void decodeImage() throws IOException {
        try {
            int bytesRead = 0;
            int needBytes, offset, bytesInBuffer = 0;
            for (;;) {
                needBytes = INPUT_BUFFER_SIZE - bytesInBuffer;
                offset = bytesInBuffer;
                bytesRead = inputStream.read(buffer, offset, needBytes);
                if (bytesRead < 0) { 
                    releaseNativeDecoder(hNativeDecoder);
                    break;
                }
                bytesInBuffer += bytesRead;
                hNativeDecoder = decode(buffer, bytesInBuffer, hNativeDecoder);
                bytesInBuffer = 0;
                returnData();
                if (hNativeDecoder == 0) {
                    break;
                }
            }
            imageComplete(ImageConsumer.STATICIMAGEDONE);
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            imageComplete(ImageConsumer.IMAGEERROR);
            throw e;
        } finally {
            closeStream();
        }
    }
    @SuppressWarnings("unused")
    private void returnHeader() { 
        setDimensions(imageWidth, imageHeight);
        switch (colorType) {
            case PNG_COLOR_TYPE_GRAY: {
                if (bitDepth != 8 && bitDepth != 4 && bitDepth != 2 && bitDepth != 1) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                int numEntries = 1 << bitDepth;
                int scaleFactor = 255 / (numEntries-1);
                byte comps[] = new byte[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    comps[i] = (byte) (i * scaleFactor);
                }
                cm = new IndexColorModel(8, numEntries, comps, comps, comps);
                transferInts = false;
                break;
            }
            case PNG_COLOR_TYPE_RGB: {
                if (bitDepth != 8) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                cm = new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
                transferInts = true;
                break;
            }
            case PNG_COLOR_TYPE_PLTE: {
                if (bitDepth != 8 && bitDepth != 4 && bitDepth != 2 && bitDepth != 1) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                cm = new IndexColorModel(8, cmap.length / 3, cmap, 0, false);
                transferInts = false;
                break;
            }
            case PNG_COLOR_TYPE_GRAY_ALPHA: {
                if (bitDepth != 8) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        true, false,
                        Transparency.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE);
                transferInts = false;
                dataElementsPerPixel = 2;
                break;
            }
            case PNG_COLOR_TYPE_RGBA: {
                if (bitDepth != 8) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                cm = ColorModel.getRGBdefault();
                transferInts = true;
                break;
            }
            default:
                throw new IllegalArgumentException(Messages.getString("awt.3C")); 
        }
        if (transferInts) {
            intOut = new int[imageWidth * imageHeight];
        } else {
            byteOut = new byte[imageWidth * imageHeight * dataElementsPerPixel];
        }
        setColorModel(cm);
        setHints(hintflags);
        setProperties(new Hashtable<Object, Object>()); 
    }
    private void returnData() {
        if (numScanlines > 0) {
            int pass1, pass2;
            if (updateFromScanline + numScanlines > imageHeight) {
                pass1 = imageHeight - updateFromScanline;
                pass2 = updateFromScanline + numScanlines - imageHeight;
            } else {
                pass1 = numScanlines;
                pass2 = 0;
            }
            transfer(updateFromScanline, pass1);
            if (pass2 != 0) {
                transfer(0, pass2);
            }
        }
    }
    private void transfer(int updateFromScanline, int numScanlines) {
        if (transferInts) {
            setPixels(
                    0, updateFromScanline,
                    imageWidth, numScanlines,
                    cm, intOut,
                    updateFromScanline * imageWidth,
                    imageWidth
            );
        } else {
            setPixels(
                    0, updateFromScanline,
                    imageWidth, numScanlines,
                    cm, byteOut,
                    updateFromScanline * imageWidth * dataElementsPerPixel,
                    imageWidth * dataElementsPerPixel
            );
        }
    }
}
