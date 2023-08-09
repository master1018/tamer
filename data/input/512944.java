public abstract class ImageReader {
    protected ImageReaderSpi originatingProvider;
    protected Object input;
    protected boolean seekForwardOnly;
    protected boolean ignoreMetadata;
    protected int minIndex;
    protected Locale[] availableLocales;
    protected Locale locale;
    protected List<IIOReadWarningListener> warningListeners;
    protected List<Locale> warningLocales;
    protected List<IIOReadProgressListener> progressListeners;
    protected List<IIOReadUpdateListener> updateListeners;
    protected ImageReader(ImageReaderSpi originatingProvider) {
        this.originatingProvider = originatingProvider;
    }
    public String getFormatName() throws IOException {
        return originatingProvider.getFormatNames()[0];
    }
    public ImageReaderSpi getOriginatingProvider() {
        return originatingProvider;
    }
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        if (input != null) {
            if (!isSupported(input) && !(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException("input " + input + " is not supported");
            }
        }
        this.minIndex = 0;
        this.seekForwardOnly = seekForwardOnly;
        this.ignoreMetadata = ignoreMetadata;
        this.input = input;
    }
    private boolean isSupported(Object input) {
        ImageReaderSpi spi = getOriginatingProvider();
        if (null != spi) {
            Class[] outTypes = spi.getInputTypes();
            for (Class<?> element : outTypes) {
                if (element.isInstance(input)) {
                    return true;
                }
            }
        }
        return false;
    }
    public void setInput(Object input, boolean seekForwardOnly) {
        setInput(input, seekForwardOnly, false);
    }
    public void setInput(Object input) {
        setInput(input, false, false);
    }
    public Object getInput() {
        return input;
    }
    public boolean isSeekForwardOnly() {
        return seekForwardOnly;
    }
    public boolean isIgnoringMetadata() {
        return ignoreMetadata;
    }
    public int getMinIndex() {
        return minIndex;
    }
    public Locale[] getAvailableLocales() {
        return availableLocales;
    }
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public Locale getLocale() {
        return locale;
    }
    public abstract int getNumImages(boolean allowSearch) throws IOException;
    public abstract int getWidth(int imageIndex) throws IOException;
    public abstract int getHeight(int imageIndex) throws IOException;
    public boolean isRandomAccessEasy(int imageIndex) throws IOException {
        return false; 
    }
    public float getAspectRatio(int imageIndex) throws IOException {
        return (float)getWidth(imageIndex) / getHeight(imageIndex);
    }
    public ImageTypeSpecifier getRawImageType(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public abstract Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException;
    public ImageReadParam getDefaultReadParam() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public abstract IIOMetadata getStreamMetadata() throws IOException;
    public IIOMetadata getStreamMetadata(String formatName, Set<String> nodeNames)
            throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public abstract IIOMetadata getImageMetadata(int imageIndex) throws IOException;
    public IIOMetadata getImageMetadata(int imageIndex, String formatName, Set<String> nodeNames)
            throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public BufferedImage read(int imageIndex) throws IOException {
        return read(imageIndex, null);
    }
    public abstract BufferedImage read(int imageIndex, ImageReadParam param) throws IOException;
    public IIOImage readAll(int imageIndex, ImageReadParam param) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public Iterator<IIOImage> readAll(Iterator<? extends ImageReadParam> params) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public boolean canReadRaster() {
        return false; 
    }
    public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
        throw new UnsupportedOperationException("Unsupported");
    }
    public boolean isImageTiled(int imageIndex) throws IOException {
        return false; 
    }
    public int getTileWidth(int imageIndex) throws IOException {
        return getWidth(imageIndex); 
    }
    public int getTileHeight(int imageIndex) throws IOException {
        return getHeight(imageIndex); 
    }
    public int getTileGridXOffset(int imageIndex) throws IOException {
        return 0; 
    }
    public int getTileGridYOffset(int imageIndex) throws IOException {
        return 0; 
    }
    public BufferedImage readTile(int imageIndex, int tileX, int tileY) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public Raster readTileRaster(int imageIndex, int tileX, int tileY) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public RenderedImage readAsRenderedImage(int imageIndex, ImageReadParam param)
            throws IOException {
        return read(imageIndex, param);
    }
    public boolean readerSupportsThumbnails() {
        return false; 
    }
    public boolean hasThumbnails(int imageIndex) throws IOException {
        return getNumThumbnails(imageIndex) > 0; 
    }
    public int getNumThumbnails(int imageIndex) throws IOException {
        return 0; 
    }
    public int getThumbnailWidth(int imageIndex, int thumbnailIndex) throws IOException {
        return readThumbnail(imageIndex, thumbnailIndex).getWidth(); 
    }
    public int getThumbnailHeight(int imageIndex, int thumbnailIndex) throws IOException {
        return readThumbnail(imageIndex, thumbnailIndex).getHeight(); 
    }
    public BufferedImage readThumbnail(int imageIndex, int thumbnailIndex) throws IOException {
        throw new UnsupportedOperationException("Unsupported"); 
    }
    public void abort() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected boolean abortRequested() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void clearAbortRequest() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void addIIOReadWarningListener(IIOReadWarningListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeIIOReadWarningListener(IIOReadWarningListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeAllIIOReadWarningListeners() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void addIIOReadProgressListener(IIOReadProgressListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeIIOReadProgressListener(IIOReadProgressListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeAllIIOReadProgressListeners() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void addIIOReadUpdateListener(IIOReadUpdateListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeIIOReadUpdateListener(IIOReadUpdateListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void removeAllIIOReadUpdateListeners() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processSequenceStarted(int minIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processSequenceComplete() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processImageStarted(int imageIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processImageProgress(float percentageDone) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processImageComplete() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailStarted(int imageIndex, int thumbnailIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailProgress(float percentageDone) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailComplete() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processReadAborted() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processPassStarted(BufferedImage theImage, int pass, int minPass, int maxPass,
            int minX, int minY, int periodX, int periodY, int[] bands) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processImageUpdate(BufferedImage theImage, int minX, int minY, int width,
            int height, int periodX, int periodY, int[] bands) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processPassComplete(BufferedImage theImage) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailPassStarted(BufferedImage theThumbnail, int pass, int minPass,
            int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailUpdate(BufferedImage theThumbnail, int minX, int minY,
            int width, int height, int periodX, int periodY, int[] bands) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processThumbnailPassComplete(BufferedImage theThumbnail) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processWarningOccurred(String warning) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected void processWarningOccurred(String baseName, String keyword) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void reset() {
        setInput(null, false);
        setLocale(null);
        removeAllIIOReadUpdateListeners();
        removeAllIIOReadWarningListeners();
        removeAllIIOReadProgressListeners();
        clearAbortRequest();
    }
    public void dispose() {
    }
    protected static Rectangle getSourceRegion(ImageReadParam param, int srcWidth, int srcHeight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected static void computeRegions(ImageReadParam param, int srcWidth, int srcHeight,
            BufferedImage image, Rectangle srcRegion, Rectangle destRegion) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected static void checkReadParamBandSettings(ImageReadParam param, int numSrcBands,
            int numDstBands) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected static BufferedImage getDestination(ImageReadParam param,
            Iterator<ImageTypeSpecifier> imageTypes, int width, int height) throws IIOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
