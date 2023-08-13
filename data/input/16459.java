public abstract class ImageReader {
    protected ImageReaderSpi originatingProvider;
    protected Object input = null;
    protected boolean seekForwardOnly = false;
    protected boolean ignoreMetadata = false;
    protected int minIndex = 0;
    protected Locale[] availableLocales = null;
    protected Locale locale = null;
    protected List<IIOReadWarningListener> warningListeners = null;
    protected List<Locale> warningLocales = null;
    protected List<IIOReadProgressListener> progressListeners = null;
    protected List<IIOReadUpdateListener> updateListeners = null;
    private boolean abortFlag = false;
    protected ImageReader(ImageReaderSpi originatingProvider) {
        this.originatingProvider = originatingProvider;
    }
    public String getFormatName() throws IOException {
        return originatingProvider.getFormatNames()[0];
    }
    public ImageReaderSpi getOriginatingProvider() {
        return originatingProvider;
    }
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        if (input != null) {
            boolean found = false;
            if (originatingProvider != null) {
                Class[] classes = originatingProvider.getInputTypes();
                for (int i = 0; i < classes.length; i++) {
                    if (classes[i].isInstance(input)) {
                        found = true;
                        break;
                    }
                }
            } else {
                if (input instanceof ImageInputStream) {
                    found = true;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Incorrect input type!");
            }
            this.seekForwardOnly = seekForwardOnly;
            this.ignoreMetadata = ignoreMetadata;
            this.minIndex = 0;
        }
        this.input = input;
    }
    public void setInput(Object input,
                         boolean seekForwardOnly) {
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
        if (availableLocales == null) {
            return null;
        } else {
            return (Locale[])availableLocales.clone();
        }
    }
    public void setLocale(Locale locale) {
        if (locale != null) {
            Locale[] locales = getAvailableLocales();
            boolean found = false;
            if (locales != null) {
                for (int i = 0; i < locales.length; i++) {
                    if (locale.equals(locales[i])) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Invalid locale!");
            }
        }
        this.locale = locale;
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
        return (float)getWidth(imageIndex)/getHeight(imageIndex);
    }
    public ImageTypeSpecifier getRawImageType(int imageIndex)
        throws IOException {
        return (ImageTypeSpecifier)getImageTypes(imageIndex).next();
    }
    public abstract Iterator<ImageTypeSpecifier>
        getImageTypes(int imageIndex) throws IOException;
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }
    public abstract IIOMetadata getStreamMetadata() throws IOException;
    public IIOMetadata getStreamMetadata(String formatName,
                                         Set<String> nodeNames)
        throws IOException
    {
        return getMetadata(formatName, nodeNames, true, 0);
    }
    private IIOMetadata getMetadata(String formatName,
                                    Set nodeNames,
                                    boolean wantStream,
                                    int imageIndex) throws IOException {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (nodeNames == null) {
            throw new IllegalArgumentException("nodeNames == null!");
        }
        IIOMetadata metadata =
            wantStream
            ? getStreamMetadata()
            : getImageMetadata(imageIndex);
        if (metadata != null) {
            if (metadata.isStandardMetadataFormatSupported() &&
                formatName.equals
                (IIOMetadataFormatImpl.standardMetadataFormatName)) {
                return metadata;
            }
            String nativeName = metadata.getNativeMetadataFormatName();
            if (nativeName != null && formatName.equals(nativeName)) {
                return metadata;
            }
            String[] extraNames = metadata.getExtraMetadataFormatNames();
            if (extraNames != null) {
                for (int i = 0; i < extraNames.length; i++) {
                    if (formatName.equals(extraNames[i])) {
                        return metadata;
                    }
                }
            }
        }
        return null;
    }
    public abstract IIOMetadata getImageMetadata(int imageIndex)
        throws IOException;
    public IIOMetadata getImageMetadata(int imageIndex,
                                        String formatName,
                                        Set<String> nodeNames)
        throws IOException {
        return getMetadata(formatName, nodeNames, false, imageIndex);
    }
    public BufferedImage read(int imageIndex) throws IOException {
        return read(imageIndex, null);
    }
    public abstract BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException;
    public IIOImage readAll(int imageIndex, ImageReadParam param)
        throws IOException {
        if (imageIndex < getMinIndex()) {
            throw new IndexOutOfBoundsException("imageIndex < getMinIndex()!");
        }
        BufferedImage im = read(imageIndex, param);
        ArrayList thumbnails = null;
        int numThumbnails = getNumThumbnails(imageIndex);
        if (numThumbnails > 0) {
            thumbnails = new ArrayList();
            for (int j = 0; j < numThumbnails; j++) {
                thumbnails.add(readThumbnail(imageIndex, j));
            }
        }
        IIOMetadata metadata = getImageMetadata(imageIndex);
        return new IIOImage(im, thumbnails, metadata);
    }
    public Iterator<IIOImage>
        readAll(Iterator<? extends ImageReadParam> params)
        throws IOException
    {
        List output = new ArrayList();
        int imageIndex = getMinIndex();
        processSequenceStarted(imageIndex);
        while (true) {
            ImageReadParam param = null;
            if (params != null && params.hasNext()) {
                Object o = params.next();
                if (o != null) {
                    if (o instanceof ImageReadParam) {
                        param = (ImageReadParam)o;
                    } else {
                        throw new IllegalArgumentException
                            ("Non-ImageReadParam supplied as part of params!");
                    }
                }
            }
            BufferedImage bi = null;
            try {
                bi = read(imageIndex, param);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
            ArrayList thumbnails = null;
            int numThumbnails = getNumThumbnails(imageIndex);
            if (numThumbnails > 0) {
                thumbnails = new ArrayList();
                for (int j = 0; j < numThumbnails; j++) {
                    thumbnails.add(readThumbnail(imageIndex, j));
                }
            }
            IIOMetadata metadata = getImageMetadata(imageIndex);
            IIOImage im = new IIOImage(bi, thumbnails, metadata);
            output.add(im);
            ++imageIndex;
        }
        processSequenceComplete();
        return output.iterator();
    }
    public boolean canReadRaster() {
        return false;
    }
    public Raster readRaster(int imageIndex, ImageReadParam param)
        throws IOException {
        throw new UnsupportedOperationException("readRaster not supported!");
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
    public BufferedImage readTile(int imageIndex,
                                  int tileX, int tileY) throws IOException {
        if ((tileX != 0) || (tileY != 0)) {
            throw new IllegalArgumentException("Invalid tile indices");
        }
        return read(imageIndex);
    }
    public Raster readTileRaster(int imageIndex,
                                 int tileX, int tileY) throws IOException {
        if (!canReadRaster()) {
            throw new UnsupportedOperationException
                ("readTileRaster not supported!");
        }
        if ((tileX != 0) || (tileY != 0)) {
            throw new IllegalArgumentException("Invalid tile indices");
        }
        return readRaster(imageIndex, null);
    }
    public RenderedImage readAsRenderedImage(int imageIndex,
                                             ImageReadParam param)
        throws IOException {
        return read(imageIndex, param);
    }
    public boolean readerSupportsThumbnails() {
        return false;
    }
    public boolean hasThumbnails(int imageIndex) throws IOException {
        return getNumThumbnails(imageIndex) > 0;
    }
    public int getNumThumbnails(int imageIndex)
        throws IOException {
        return 0;
    }
    public int getThumbnailWidth(int imageIndex, int thumbnailIndex)
        throws IOException {
        return readThumbnail(imageIndex, thumbnailIndex).getWidth();
    }
    public int getThumbnailHeight(int imageIndex, int thumbnailIndex)
        throws IOException {
        return readThumbnail(imageIndex, thumbnailIndex).getHeight();
    }
    public BufferedImage readThumbnail(int imageIndex,
                                       int thumbnailIndex)
        throws IOException {
        throw new UnsupportedOperationException("Thumbnails not supported!");
    }
    public synchronized void abort() {
        this.abortFlag = true;
    }
    protected synchronized boolean abortRequested() {
        return this.abortFlag;
    }
    protected synchronized void clearAbortRequest() {
        this.abortFlag = false;
    }
    static List addToList(List l, Object elt) {
        if (l == null) {
            l = new ArrayList();
        }
        l.add(elt);
        return l;
    }
    static List removeFromList(List l, Object elt) {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }
    public void addIIOReadWarningListener(IIOReadWarningListener listener) {
        if (listener == null) {
            return;
        }
        warningListeners = addToList(warningListeners, listener);
        warningLocales = addToList(warningLocales, getLocale());
    }
    public void removeIIOReadWarningListener(IIOReadWarningListener listener) {
        if (listener == null || warningListeners == null) {
            return;
        }
        int index = warningListeners.indexOf(listener);
        if (index != -1) {
            warningListeners.remove(index);
            warningLocales.remove(index);
            if (warningListeners.size() == 0) {
                warningListeners = null;
                warningLocales = null;
            }
        }
    }
    public void removeAllIIOReadWarningListeners() {
        warningListeners = null;
        warningLocales = null;
    }
    public void addIIOReadProgressListener(IIOReadProgressListener listener) {
        if (listener == null) {
            return;
        }
        progressListeners = addToList(progressListeners, listener);
    }
    public void
        removeIIOReadProgressListener (IIOReadProgressListener listener) {
        if (listener == null || progressListeners == null) {
            return;
        }
        progressListeners = removeFromList(progressListeners, listener);
    }
    public void removeAllIIOReadProgressListeners() {
        progressListeners = null;
    }
    public void
        addIIOReadUpdateListener(IIOReadUpdateListener listener) {
        if (listener == null) {
            return;
        }
        updateListeners = addToList(updateListeners, listener);
    }
    public void removeIIOReadUpdateListener(IIOReadUpdateListener listener) {
        if (listener == null || updateListeners == null) {
            return;
        }
        updateListeners = removeFromList(updateListeners, listener);
    }
    public void removeAllIIOReadUpdateListeners() {
        updateListeners = null;
    }
    protected void processSequenceStarted(int minIndex) {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.sequenceStarted(this, minIndex);
        }
    }
    protected void processSequenceComplete() {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.sequenceComplete(this);
        }
    }
    protected void processImageStarted(int imageIndex) {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.imageStarted(this, imageIndex);
        }
    }
    protected void processImageProgress(float percentageDone) {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.imageProgress(this, percentageDone);
        }
    }
    protected void processImageComplete() {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.imageComplete(this);
        }
    }
    protected void processThumbnailStarted(int imageIndex,
                                           int thumbnailIndex) {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.thumbnailStarted(this, imageIndex, thumbnailIndex);
        }
    }
    protected void processThumbnailProgress(float percentageDone) {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.thumbnailProgress(this, percentageDone);
        }
    }
    protected void processThumbnailComplete() {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.thumbnailComplete(this);
        }
    }
    protected void processReadAborted() {
        if (progressListeners == null) {
            return;
        }
        int numListeners = progressListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadProgressListener listener =
                (IIOReadProgressListener)progressListeners.get(i);
            listener.readAborted(this);
        }
    }
    protected void processPassStarted(BufferedImage theImage,
                                      int pass,
                                      int minPass, int maxPass,
                                      int minX, int minY,
                                      int periodX, int periodY,
                                      int[] bands) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.passStarted(this, theImage, pass,
                                 minPass,
                                 maxPass,
                                 minX, minY,
                                 periodX, periodY,
                                 bands);
        }
    }
    protected void processImageUpdate(BufferedImage theImage,
                                      int minX, int minY,
                                      int width, int height,
                                      int periodX, int periodY,
                                      int[] bands) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.imageUpdate(this,
                                 theImage,
                                 minX, minY,
                                 width, height,
                                 periodX, periodY,
                                 bands);
        }
    }
    protected void processPassComplete(BufferedImage theImage) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.passComplete(this, theImage);
        }
    }
    protected void processThumbnailPassStarted(BufferedImage theThumbnail,
                                               int pass,
                                               int minPass, int maxPass,
                                               int minX, int minY,
                                               int periodX, int periodY,
                                               int[] bands) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.thumbnailPassStarted(this, theThumbnail, pass,
                                          minPass,
                                          maxPass,
                                          minX, minY,
                                          periodX, periodY,
                                          bands);
        }
    }
    protected void processThumbnailUpdate(BufferedImage theThumbnail,
                                          int minX, int minY,
                                          int width, int height,
                                          int periodX, int periodY,
                                          int[] bands) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.thumbnailUpdate(this,
                                     theThumbnail,
                                     minX, minY,
                                     width, height,
                                     periodX, periodY,
                                     bands);
        }
    }
    protected void processThumbnailPassComplete(BufferedImage theThumbnail) {
        if (updateListeners == null) {
            return;
        }
        int numListeners = updateListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadUpdateListener listener =
                (IIOReadUpdateListener)updateListeners.get(i);
            listener.thumbnailPassComplete(this, theThumbnail);
        }
    }
    protected void processWarningOccurred(String warning) {
        if (warningListeners == null) {
            return;
        }
        if (warning == null) {
            throw new IllegalArgumentException("warning == null!");
        }
        int numListeners = warningListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadWarningListener listener =
                (IIOReadWarningListener)warningListeners.get(i);
            listener.warningOccurred(this, warning);
        }
    }
    protected void processWarningOccurred(String baseName,
                                          String keyword) {
        if (warningListeners == null) {
            return;
        }
        if (baseName == null) {
            throw new IllegalArgumentException("baseName == null!");
        }
        if (keyword == null) {
            throw new IllegalArgumentException("keyword == null!");
        }
        int numListeners = warningListeners.size();
        for (int i = 0; i < numListeners; i++) {
            IIOReadWarningListener listener =
                (IIOReadWarningListener)warningListeners.get(i);
            Locale locale = (Locale)warningLocales.get(i);
            if (locale == null) {
                locale = Locale.getDefault();
            }
            ClassLoader loader = (ClassLoader)
                java.security.AccessController.doPrivileged(
                   new java.security.PrivilegedAction() {
                      public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                      }
                });
            ResourceBundle bundle = null;
            try {
                bundle = ResourceBundle.getBundle(baseName, locale, loader);
            } catch (MissingResourceException mre) {
                try {
                    bundle = ResourceBundle.getBundle(baseName, locale);
                } catch (MissingResourceException mre1) {
                    throw new IllegalArgumentException("Bundle not found!");
                }
            }
            String warning = null;
            try {
                warning = bundle.getString(keyword);
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("Resource is not a String!");
            } catch (MissingResourceException mre) {
                throw new IllegalArgumentException("Resource is missing!");
            }
            listener.warningOccurred(this, warning);
        }
    }
    public void reset() {
        setInput(null, false, false);
        setLocale(null);
        removeAllIIOReadUpdateListeners();
        removeAllIIOReadProgressListeners();
        removeAllIIOReadWarningListeners();
        clearAbortRequest();
    }
    public void dispose() {
    }
    protected static Rectangle getSourceRegion(ImageReadParam param,
                                               int srcWidth,
                                               int srcHeight) {
        Rectangle sourceRegion = new Rectangle(0, 0, srcWidth, srcHeight);
        if (param != null) {
            Rectangle region = param.getSourceRegion();
            if (region != null) {
                sourceRegion = sourceRegion.intersection(region);
            }
            int subsampleXOffset = param.getSubsamplingXOffset();
            int subsampleYOffset = param.getSubsamplingYOffset();
            sourceRegion.x += subsampleXOffset;
            sourceRegion.y += subsampleYOffset;
            sourceRegion.width -= subsampleXOffset;
            sourceRegion.height -= subsampleYOffset;
        }
        return sourceRegion;
    }
    protected static void computeRegions(ImageReadParam param,
                                         int srcWidth,
                                         int srcHeight,
                                         BufferedImage image,
                                         Rectangle srcRegion,
                                         Rectangle destRegion) {
        if (srcRegion == null) {
            throw new IllegalArgumentException("srcRegion == null!");
        }
        if (destRegion == null) {
            throw new IllegalArgumentException("destRegion == null!");
        }
        srcRegion.setBounds(0, 0, srcWidth, srcHeight);
        destRegion.setBounds(0, 0, srcWidth, srcHeight);
        int periodX = 1;
        int periodY = 1;
        int gridX = 0;
        int gridY = 0;
        if (param != null) {
            Rectangle paramSrcRegion = param.getSourceRegion();
            if (paramSrcRegion != null) {
                srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
            }
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
            gridX = param.getSubsamplingXOffset();
            gridY = param.getSubsamplingYOffset();
            srcRegion.translate(gridX, gridY);
            srcRegion.width -= gridX;
            srcRegion.height -= gridY;
            destRegion.setLocation(param.getDestinationOffset());
        }
        if (destRegion.x < 0) {
            int delta = -destRegion.x*periodX;
            srcRegion.x += delta;
            srcRegion.width -= delta;
            destRegion.x = 0;
        }
        if (destRegion.y < 0) {
            int delta = -destRegion.y*periodY;
            srcRegion.y += delta;
            srcRegion.height -= delta;
            destRegion.y = 0;
        }
        int subsampledWidth = (srcRegion.width + periodX - 1)/periodX;
        int subsampledHeight = (srcRegion.height + periodY - 1)/periodY;
        destRegion.width = subsampledWidth;
        destRegion.height = subsampledHeight;
        if (image != null) {
            Rectangle destImageRect = new Rectangle(0, 0,
                                                    image.getWidth(),
                                                    image.getHeight());
            destRegion.setBounds(destRegion.intersection(destImageRect));
            if (destRegion.isEmpty()) {
                throw new IllegalArgumentException
                    ("Empty destination region!");
            }
            int deltaX = destRegion.x + subsampledWidth - image.getWidth();
            if (deltaX > 0) {
                srcRegion.width -= deltaX*periodX;
            }
            int deltaY =  destRegion.y + subsampledHeight - image.getHeight();
            if (deltaY > 0) {
                srcRegion.height -= deltaY*periodY;
            }
        }
        if (srcRegion.isEmpty() || destRegion.isEmpty()) {
            throw new IllegalArgumentException("Empty region!");
        }
    }
    protected static void checkReadParamBandSettings(ImageReadParam param,
                                                     int numSrcBands,
                                                     int numDstBands) {
        int[] srcBands = null;
        int[] dstBands = null;
        if (param != null) {
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        }
        int paramSrcBandLength =
            (srcBands == null) ? numSrcBands : srcBands.length;
        int paramDstBandLength =
            (dstBands == null) ? numDstBands : dstBands.length;
        if (paramSrcBandLength != paramDstBandLength) {
            throw new IllegalArgumentException("ImageReadParam num source & dest bands differ!");
        }
        if (srcBands != null) {
            for (int i = 0; i < srcBands.length; i++) {
                if (srcBands[i] >= numSrcBands) {
                    throw new IllegalArgumentException("ImageReadParam source bands contains a value >= the number of source bands!");
                }
            }
        }
        if (dstBands != null) {
            for (int i = 0; i < dstBands.length; i++) {
                if (dstBands[i] >= numDstBands) {
                    throw new IllegalArgumentException("ImageReadParam dest bands contains a value >= the number of dest bands!");
                }
            }
        }
    }
    protected static BufferedImage
        getDestination(ImageReadParam param,
                       Iterator<ImageTypeSpecifier> imageTypes,
                       int width, int height)
        throws IIOException {
        if (imageTypes == null || !imageTypes.hasNext()) {
            throw new IllegalArgumentException("imageTypes null or empty!");
        }
        if ((long)width*height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                ("width*height > Integer.MAX_VALUE!");
        }
        BufferedImage dest = null;
        ImageTypeSpecifier imageType = null;
        if (param != null) {
            dest = param.getDestination();
            if (dest != null) {
                return dest;
            }
            imageType = param.getDestinationType();
        }
        if (imageType == null) {
            Object o = imageTypes.next();
            if (!(o instanceof ImageTypeSpecifier)) {
                throw new IllegalArgumentException
                    ("Non-ImageTypeSpecifier retrieved from imageTypes!");
            }
            imageType = (ImageTypeSpecifier)o;
        } else {
            boolean foundIt = false;
            while (imageTypes.hasNext()) {
                ImageTypeSpecifier type =
                    (ImageTypeSpecifier)imageTypes.next();
                if (type.equals(imageType)) {
                    foundIt = true;
                    break;
                }
            }
            if (!foundIt) {
                throw new IIOException
                    ("Destination type from ImageReadParam does not match!");
            }
        }
        Rectangle srcRegion = new Rectangle(0,0,0,0);
        Rectangle destRegion = new Rectangle(0,0,0,0);
        computeRegions(param,
                       width,
                       height,
                       null,
                       srcRegion,
                       destRegion);
        int destWidth = destRegion.x + destRegion.width;
        int destHeight = destRegion.y + destRegion.height;
        return imageType.createBufferedImage(destWidth, destHeight);
    }
}
