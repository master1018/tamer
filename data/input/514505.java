public class JPEGImageWriter extends ImageWriter {
    private static final boolean DEBUG = false;
    private static Bitmap bm;
    public static Bitmap getBitmap() {
        return bm;
    }
    private static BufferedImage bufImg;
    public static BufferedImage getBufImage() {
        return bufImg;
    }
    static private RenderedImage renImg;
    static public RenderedImage getRenImage() {
        return renImg;
    }
    private long cinfo;
    private RenderedImage image;
    private Raster sourceRaster;
    private WritableRaster scanRaster;
    private int srcXOff = 0;
    private int srcYOff = 0;
    private int srcWidth;
    private int srcHeight;
    private int deltaY = 1;
    private int deltaX = 1;
    private ImageOutputStream ios;
    public JPEGImageWriter(ImageWriterSpi imageWriterSpi) {
        super(imageWriterSpi);
        cinfo = System.currentTimeMillis();
    }
    static {
    }
    @Override
    public void write(IIOMetadata iioMetadata, IIOImage iioImage, ImageWriteParam param)
            throws IOException {
        if (ios == null) {
            throw new IllegalArgumentException("ios == null");
        }
        if (iioImage == null) {
            throw new IllegalArgumentException("Image equals null");
        }
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
        if (DEBUG) {
            if( img==null ) {
                System.out.println("****J: Image is NULL");
            } else {
                renImg = img;
                bufImg = (BufferedImage)img;
            }
        }
        int numBands = sourceRaster.getNumBands();
        int sourceIJGCs = img == null ? JPEGConsts.JCS_UNKNOW : getSourceCSType(img);
        srcWidth = sourceRaster.getWidth();
        srcHeight = sourceRaster.getHeight();
        int destWidth = srcWidth;
        int destHeight = srcHeight;
        boolean progressive = false;
        if (param != null) {
            Rectangle reg = param.getSourceRegion();
            if (reg != null) {
                srcXOff = reg.x;
                srcYOff = reg.y;
                srcWidth = reg.width + srcXOff > srcWidth
                        ? srcWidth - srcXOff
                        : reg.width;
                srcHeight = reg.height + srcYOff > srcHeight
                        ? srcHeight - srcYOff
                        : reg.height;
            }
            deltaX = param.getSourceXSubsampling();
            deltaY = param.getSourceYSubsampling();
            int offsetX = param.getSubsamplingXOffset();
            int offsetY = param.getSubsamplingYOffset();
            srcXOff += offsetX;
            srcYOff += offsetY;
            srcWidth -= offsetX;
            srcHeight -= offsetY;
            destWidth = (srcWidth + deltaX - 1) / deltaX;
            destHeight = (srcHeight + deltaY - 1) / deltaY;
        }
        SampleModel model = sourceRaster.getSampleModel();
        if (model instanceof SinglePixelPackedSampleModel) {
            DataBufferInt ibuf = (DataBufferInt)sourceRaster.getDataBuffer();
            int[] pixels = ibuf.getData();
            bm = Bitmap.createBitmap(pixels, srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
            ImageOutputStreamWrapper iosw = new ImageOutputStreamWrapper(ios);
            bm.compress(CompressFormat.JPEG, 100, iosw);
        } else {
            throw new RuntimeException("Color model not supported yet");
        }
    }
    @Override
    public void dispose() {
        super.dispose();
        if (cinfo != 0) {
            cinfo = 0;
            ios = null;
        }
    }
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam imageWriteParam) {
        throw new UnsupportedOperationException("not supported yet");
    }
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageTypeSpecifier, ImageWriteParam imageWriteParam) {
        throw new UnsupportedOperationException("not supported yet");
    }
    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata iioMetadata, ImageWriteParam imageWriteParam) {
        throw new UnsupportedOperationException("not supported yet");
    }
    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata iioMetadata, ImageTypeSpecifier imageTypeSpecifier, ImageWriteParam imageWriteParam) {
        throw new UnsupportedOperationException("not supported yet");
    }
    @Override
    public void setOutput(Object output) {
        super.setOutput(output);
        ios = (ImageOutputStream) output;
        sourceRaster = null;
        scanRaster = null;
        srcXOff = 0;
        srcYOff = 0;
        srcWidth = 0;
        srcHeight = 0;
        deltaY = 1;
    }
    @SuppressWarnings("unused")
    private void getScanLine(int scanline) {
        Raster child = sourceRaster.createChild(srcXOff,
                srcYOff + scanline * deltaY, srcWidth, 1, 0, 0, null);
        scanRaster.setRect(child);
    }
    private int getSourceCSType(RenderedImage image) {
        int type = JPEGConsts.JCS_UNKNOW;
        ColorModel cm = image.getColorModel();
        if (null == cm) {
            return type;
        }
        if (cm instanceof IndexColorModel) {
            throw new UnsupportedOperationException("IndexColorModel is not supported yet");
        }
        boolean hasAlpha = cm.hasAlpha();
        ColorSpace cs = cm.getColorSpace();
        switch(cs.getType()) {
            case ColorSpace.TYPE_GRAY:
                type = JPEGConsts.JCS_GRAYSCALE;
                break;
           case ColorSpace.TYPE_RGB:
                type = hasAlpha ? JPEGConsts.JCS_RGBA : JPEGConsts.JCS_RGB;
                break;
           case ColorSpace.TYPE_YCbCr:
                type = hasAlpha ? JPEGConsts.JCS_YCbCrA : JPEGConsts.JCS_YCbCr;
                break;
           case ColorSpace.TYPE_3CLR:
                 type = hasAlpha ? JPEGConsts.JCS_YCCA : JPEGConsts.JCS_YCC;
                 break;
           case ColorSpace.TYPE_CMYK:
                  type = JPEGConsts.JCS_CMYK;
                  break;
        }
        return type;
    }
    private int getDestinationCSType(RenderedImage image) {
        int type = JPEGConsts.JCS_UNKNOW;
        ColorModel cm = image.getColorModel();
        if (null != cm) {
            boolean hasAlpha = cm.hasAlpha();
            ColorSpace cs = cm.getColorSpace();
            switch(cs.getType()) {
                case ColorSpace.TYPE_GRAY:
                    type = JPEGConsts.JCS_GRAYSCALE;
                    break;
               case ColorSpace.TYPE_RGB:
                    type = hasAlpha ? JPEGConsts.JCS_YCbCrA : JPEGConsts.JCS_YCbCr;
                    break;
               case ColorSpace.TYPE_YCbCr:
                    type = hasAlpha ? JPEGConsts.JCS_YCbCrA : JPEGConsts.JCS_YCbCr;
                    break;
               case ColorSpace.TYPE_3CLR:
                     type = hasAlpha ? JPEGConsts.JCS_YCCA : JPEGConsts.JCS_YCC;
                     break;
               case ColorSpace.TYPE_CMYK:
                      type = JPEGConsts.JCS_CMYK;
                      break;
            }
        }
        return type;
    }
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGImageWriteParam(getLocale());
    }
}
