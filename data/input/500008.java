public class ImageTypeSpecifier {
    protected ColorModel colorModel;
    protected SampleModel sampleModel;
    public ImageTypeSpecifier(ColorModel colorModel, SampleModel sampleModel) {
        if (colorModel == null) {
            throw new IllegalArgumentException("color model should not be NULL");
        }
        if (sampleModel == null) {
            throw new IllegalArgumentException("sample model should not be NULL");
        }
        if (!colorModel.isCompatibleSampleModel(sampleModel)) {
            throw new IllegalArgumentException("color and sample models are not compatible");
        }
        this.colorModel = colorModel;
        this.sampleModel = sampleModel;
    }
    public ImageTypeSpecifier(RenderedImage renderedImage) {
        if (renderedImage == null) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        this.colorModel = renderedImage.getColorModel();
        this.sampleModel = renderedImage.getSampleModel();
    }
    public static ImageTypeSpecifier createPacked(ColorSpace colorSpace, int redMask,
            int greenMask, int blueMask, int alphaMask, int transferType,
            boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createInterleaved(ColorSpace colorSpace, int[] bandOffsets,
            int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createBanded(ColorSpace colorSpace, int[] bankIndices,
            int[] bandOffsets, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned,
            boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createIndexed(byte[] redLUT, byte[] greenLUT, byte[] blueLUT,
            byte[] alphaLUT, int bits, int dataType) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createFromBufferedImageType(int bufferedImageType) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public static ImageTypeSpecifier createFromRenderedImage(RenderedImage image) {
        if (null == image) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        return new ImageTypeSpecifier(image);
    }
    public int getBufferedImageType() {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public int getNumComponents() {
        return colorModel.getNumComponents();
    }
    public int getNumBands() {
        return sampleModel.getNumBands();
    }
    public int getBitsPerBand(int band) {
        if (band < 0 || band >= getNumBands()) {
            throw new IllegalArgumentException();
        }
        return sampleModel.getSampleSize(band);
    }
    public SampleModel getSampleModel() {
        return sampleModel;
    }
    public SampleModel getSampleModel(int width, int height) {
        if ((long)width * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("width * height > Integer.MAX_VALUE");
        }
        return sampleModel.createCompatibleSampleModel(width, height);
    }
    public ColorModel getColorModel() {
        return colorModel;
    }
    public BufferedImage createBufferedImage(int width, int height) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    @Override
    public boolean equals(Object o) {
        boolean rt = false;
        if (o instanceof ImageTypeSpecifier) {
            ImageTypeSpecifier ts = (ImageTypeSpecifier)o;
            rt = colorModel.equals(ts.colorModel) && sampleModel.equals(ts.sampleModel);
        }
        return rt;
    }
}