public class AndroidImageDecoder extends ImageDecoder {
    private static final int hintflags =
        ImageConsumer.SINGLEFRAME | 
        ImageConsumer.TOPDOWNLEFTRIGHT | 
        ImageConsumer.COMPLETESCANLINES; 
    private static final int PNG_COLOR_TYPE_GRAY = 0;
    private static final int PNG_COLOR_TYPE_RGB = 2;
    private static final int PNG_COLOR_TYPE_PLTE = 3;
    private static final int PNG_COLOR_TYPE_GRAY_ALPHA = 4;
    private static final int PNG_COLOR_TYPE_RGBA = 6;
    private static final int NB_OF_LINES_PER_CHUNK = 1;  
    Bitmap bm;  
    int imageWidth; 
    int imageHeight;  
    int colorType;  
    int bitDepth;   
    byte cmap[];    
    ColorModel model;  
    boolean transferInts; 
    int dataElementsPerPixel;
    byte byteOut[];
    int intOut[];
    public AndroidImageDecoder(DecodingImageSource src, InputStream is) {
        super(src, is);
        dataElementsPerPixel = 1;
    }
    @Override
    public void decodeImage() throws IOException {
        try {
            bm = BitmapFactory.decodeStream(inputStream);
            if (bm == null) {
                throw new IOException("Input stream empty and no image cached");
            }
            imageWidth = bm.getWidth();
            imageHeight = bm.getHeight();
            if (imageWidth < 0 || imageHeight < 0 ) {
                throw new RuntimeException("Illegal image size: " 
                        + imageWidth + ", " + imageHeight);
            }
            setDimensions(imageWidth, imageHeight);
            model = createColorModel();
            setColorModel(model);
            setHints(hintflags);
            setProperties(new Hashtable<Object, Object>()); 
            sendPixels(NB_OF_LINES_PER_CHUNK != 0 ? NB_OF_LINES_PER_CHUNK : imageHeight);
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
    private ColorModel createColorModel() {
        ColorModel cm = null;
        int bmModel = 5; 
        cmap = null;
        switch (bmModel) {
        case 1: 
            colorType = PNG_COLOR_TYPE_GRAY;
            bitDepth = 1;
            break;
        case 2:
            colorType = PNG_COLOR_TYPE_GRAY_ALPHA;
            bitDepth = 8;
            break;
        case 3:
        case 4: 
        case 5: 
            colorType = bm.hasAlpha() ? PNG_COLOR_TYPE_RGBA : PNG_COLOR_TYPE_RGB;
            bitDepth = 8;
            break;
        default:
            throw new IllegalArgumentException(Messages.getString("awt.3C")); 
        }
        switch (colorType) {
            case PNG_COLOR_TYPE_GRAY: {
                if (bitDepth != 8 && bitDepth != 4 && bitDepth != 2 &&  bitDepth != 1) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                int numEntries = 1 << bitDepth;
                int scaleFactor = 255 / (numEntries-1);
                byte comps[] = new byte[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    comps[i] = (byte) (i * scaleFactor);
                }
                cm = new IndexColorModel(bitDepth, numEntries, comps, comps, comps);
                transferInts = false;
                break;
            }
            case PNG_COLOR_TYPE_RGB: {
                if (bitDepth != 8) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                cm = new DirectColorModel(24, 0xff0000, 0xFF00, 0xFF);
                transferInts = true;
                break;
            }
            case PNG_COLOR_TYPE_PLTE: {
                if (bitDepth != 8 && bitDepth != 4 && bitDepth != 2 && bitDepth != 1) {
                    throw new IllegalArgumentException(Messages.getString("awt.3C")); 
                }
                if (cmap == null) {
                    throw new IllegalStateException("Palette color type is not supported");
                }
                cm = new IndexColorModel(bitDepth, cmap.length / 3, cmap, 0, false);
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
        return cm;
    }
    private void sendPixels(int nbOfLinesPerChunk) {
        int w = imageWidth;
        int h = imageHeight;
        int n = 1;
        if (nbOfLinesPerChunk > 0 && nbOfLinesPerChunk <= h) {
            n = nbOfLinesPerChunk;
        }
        if (transferInts) {
            intOut = new int[w * n];
            for (int yi = 0; yi < h; yi += n) {
                if (n > 1 && h - yi < n ) {
                    n = h - yi;
                }
                bm.getPixels(intOut, 0, w, 0, yi, w, n);
                setPixels(0, yi, w, n, model, intOut, 0, w);
            }
        } else {
            throw new RuntimeException("Byte transfer not supported");
        }
    }
}
