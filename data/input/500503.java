public class PNGImageWriter extends ImageWriter {
    private static final boolean DEBUG = false;
    private static Bitmap bm;
    public static Bitmap getBitmap() {
        return bm;
    }
    private static int[][] BAND_OFFSETS = {
            {}, {
                0 }, {
                    0, 1 }, {
                    0, 1, 2 }, {
                    0, 1, 2, 3 } };
    private static final int PNG_COLOR_TYPE_GRAY = 0;
    private static final int PNG_COLOR_TYPE_RGB = 2;
    private static final int PNG_COLOR_TYPE_PLTE = 3;
    private static final int PNG_COLOR_TYPE_GRAY_ALPHA = 4;
    private static final int PNG_COLOR_TYPE_RGBA = 6;
    static {
    }
    protected PNGImageWriter(ImageWriterSpi iwSpi) {
        super(iwSpi);
    }
    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata arg0, ImageWriteParam arg1) {
        throw new NotImplementedException();
    }
    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata arg0, ImageTypeSpecifier arg1, ImageWriteParam arg2) {
        throw new NotImplementedException();
    }
    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier arg0, ImageWriteParam arg1) {
        throw new NotImplementedException();
    }
    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam arg0) {
        throw new NotImplementedException();
    }
    @Override
    public void write(IIOMetadata streamMetadata, IIOImage iioImage, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("Output not been set");
        }
        if (iioImage == null) {
            throw new IllegalArgumentException("Image equals null");
        }
        Raster sourceRaster;
        RenderedImage img = null;
        if (!iioImage.hasRaster()) {
            img = iioImage.getRenderedImage();
            if (img instanceof BufferedImage) {
                sourceRaster = ((BufferedImage) img).getRaster();
            } else {
                sourceRaster = img.getData();
            }
        } else {
            sourceRaster = iioImage.getRaster();
        }
        SampleModel model = sourceRaster.getSampleModel();
        int srcWidth = sourceRaster.getWidth();
        int srcHeight = sourceRaster.getHeight();
        int numBands = model.getNumBands();
        ColorModel colorModel = img.getColorModel();
        int pixelSize = colorModel.getPixelSize();
        int bytePixelSize = pixelSize / 8;
        int bitDepth = pixelSize / numBands;
        int bpb = bitDepth > 8 ? 2 : 1;
        boolean isInterlace = true;
        if (param instanceof PNGImageWriterParam) {
            isInterlace = ((PNGImageWriterParam) param).getInterlace();
        }
        int colorType = PNG_COLOR_TYPE_GRAY;
        int[] palette = null;
        if (colorModel instanceof IndexColorModel) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8) {
                throw new IllegalArgumentException(Messages.getString("imageio.1"));
            }
            if (numBands != 1) {
                throw new IllegalArgumentException(Messages.getString("imageio.1"));
            }
            IndexColorModel icm = (IndexColorModel) colorModel;
            palette = new int[icm.getMapSize()];
            icm.getRGBs(palette);
            colorType = PNG_COLOR_TYPE_PLTE;
        }
        else if (numBands == 1) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException(Messages.getString("imageio.1"));
            }
            colorType = PNG_COLOR_TYPE_GRAY;
        }
        else if (numBands == 2) {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException(Messages.getString("imageio.1"));
            }
            colorType = PNG_COLOR_TYPE_GRAY_ALPHA;
        }
        else if (numBands == 3) {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException(Messages.getString("imageio.1")); 
            }
            colorType = PNG_COLOR_TYPE_RGB;
        }
        else if (numBands == 4) {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException(Messages.getString("imageio.1")); 
            }
            colorType = PNG_COLOR_TYPE_RGBA;
        }
        if (DEBUG) {
            System.out.println("**** raster:" + sourceRaster);        
            System.out.println("**** model:" + model);
            System.out.println("**** type:" + colorType);
        }
        if (model instanceof SinglePixelPackedSampleModel) {
            DataBufferInt ibuf = (DataBufferInt)sourceRaster.getDataBuffer();
            int[] pixels = ibuf.getData();
            bm = Bitmap.createBitmap(pixels, srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
            ImageOutputStream ios = (ImageOutputStream) getOutput();
            ImageOutputStreamWrapper iosw = new ImageOutputStreamWrapper(ios);
            bm.compress(CompressFormat.PNG, 100, iosw);
        } else {
            throw new RuntimeException("Color model not supported yet");
        }
    }
    public ImageWriteParam getDefaultWriteParam() {
        return new PNGImageWriterParam();
    }
}
