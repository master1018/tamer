public class PNGImageReader  extends ImageReader {
    ImageInputStream iis;
    public PNGImageReader(ImageReaderSpi imageReaderSpi) {
        super(imageReaderSpi);
    }
    public int getNumImages(boolean allowSearch) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public int getWidth(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public int getHeight(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public BufferedImage read(int i, ImageReadParam imageReadParam) throws IOException {
        if (iis == null) {
            throw new IllegalArgumentException("input stream == null");
        }
        DecodingImageSource source = new IISDecodingImageSource(iis);
        OffscreenImage image = new OffscreenImage(source);
        source.addConsumer(image);
        source.load();
        Thread.interrupted();
        return image.getBufferedImage();
    }
    @Override
    public BufferedImage read(int i) throws IOException {
        return read(i, null);
    }
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        iis = (ImageInputStream) input;
    }
    @Override
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }
}
