public class JPEGImageReader extends ImageReader {
    ImageInputStream iis;
    public JPEGImageReader(ImageReaderSpi imageReaderSpi) {
        super(imageReaderSpi);
    }
    @Override
    public int getHeight(int i) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public int getWidth(int i) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public int getNumImages(boolean b) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int i) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }
    @Override
    public IIOMetadata getImageMetadata(int i) throws IOException {
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
        return new JPEGImageReadParam();
    }
}
