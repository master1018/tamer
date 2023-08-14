public final class ImageIO {
    private static final IIORegistry theRegistry =
        IIORegistry.getDefaultInstance();
    private ImageIO() {}
    public static void scanForPlugins() {
        theRegistry.registerApplicationClasspathSpis();
    }
    static class CacheInfo {
        boolean useCache = true;
        File cacheDirectory = null;
        Boolean hasPermission = null;
        public CacheInfo() {}
        public boolean getUseCache() {
            return useCache;
        }
        public void setUseCache(boolean useCache) {
            this.useCache = useCache;
        }
        public File getCacheDirectory() {
            return cacheDirectory;
        }
        public void setCacheDirectory(File cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
        }
        public Boolean getHasPermission() {
            return hasPermission;
        }
        public void setHasPermission(Boolean hasPermission) {
            this.hasPermission = hasPermission;
        }
    }
    private static synchronized CacheInfo getCacheInfo() {
        AppContext context = AppContext.getAppContext();
        CacheInfo info = (CacheInfo)context.get(CacheInfo.class);
        if (info == null) {
            info = new CacheInfo();
            context.put(CacheInfo.class, info);
        }
        return info;
    }
    private static String getTempDir() {
        GetPropertyAction a = new GetPropertyAction("java.io.tmpdir");
        return (String)AccessController.doPrivileged(a);
    }
    private static boolean hasCachePermission() {
        Boolean hasPermission = getCacheInfo().getHasPermission();
        if (hasPermission != null) {
            return hasPermission.booleanValue();
        } else {
            try {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    File cachedir = getCacheDirectory();
                    String cachepath;
                    if (cachedir != null) {
                        cachepath = cachedir.getPath();
                    } else {
                        cachepath = getTempDir();
                        if (cachepath == null || cachepath.isEmpty()) {
                            getCacheInfo().setHasPermission(Boolean.FALSE);
                            return false;
                        }
                    }
                    String filepath = cachepath;
                    if (!filepath.endsWith(File.separator)) {
                        filepath += File.separator;
                    }
                    filepath += "*";
                    security.checkPermission(new FilePermission(filepath, "read, write, delete"));
                }
            } catch (SecurityException e) {
                getCacheInfo().setHasPermission(Boolean.FALSE);
                return false;
            }
            getCacheInfo().setHasPermission(Boolean.TRUE);
            return true;
        }
    }
    public static void setUseCache(boolean useCache) {
        getCacheInfo().setUseCache(useCache);
    }
    public static boolean getUseCache() {
        return getCacheInfo().getUseCache();
    }
    public static void setCacheDirectory(File cacheDirectory) {
        if ((cacheDirectory != null) && !(cacheDirectory.isDirectory())) {
            throw new IllegalArgumentException("Not a directory!");
        }
        getCacheInfo().setCacheDirectory(cacheDirectory);
        getCacheInfo().setHasPermission(null);
    }
    public static File getCacheDirectory() {
        return getCacheInfo().getCacheDirectory();
    }
    public static ImageInputStream createImageInputStream(Object input)
        throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageInputStreamSpi.class,
                                                   true);
        } catch (IllegalArgumentException e) {
            return null;
        }
        boolean usecache = getUseCache() && hasCachePermission();
        while (iter.hasNext()) {
            ImageInputStreamSpi spi = (ImageInputStreamSpi)iter.next();
            if (spi.getInputClass().isInstance(input)) {
                try {
                    return spi.createInputStreamInstance(input,
                                                         usecache,
                                                         getCacheDirectory());
                } catch (IOException e) {
                    throw new IIOException("Can't create cache file!", e);
                }
            }
        }
        return null;
    }
    public static ImageOutputStream createImageOutputStream(Object output)
        throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageOutputStreamSpi.class,
                                                   true);
        } catch (IllegalArgumentException e) {
            return null;
        }
        boolean usecache = getUseCache() && hasCachePermission();
        while (iter.hasNext()) {
            ImageOutputStreamSpi spi = (ImageOutputStreamSpi)iter.next();
            if (spi.getOutputClass().isInstance(output)) {
                try {
                    return spi.createOutputStreamInstance(output,
                                                          usecache,
                                                          getCacheDirectory());
                } catch (IOException e) {
                    throw new IIOException("Can't create cache file!", e);
                }
            }
        }
        return null;
    }
    private static enum SpiInfo {
        FORMAT_NAMES {
            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getFormatNames();
            }
        },
        MIME_TYPES {
            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getMIMETypes();
            }
        },
        FILE_SUFFIXES {
            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getFileSuffixes();
            }
        };
        abstract String[] info(ImageReaderWriterSpi spi);
    }
    private static <S extends ImageReaderWriterSpi>
        String[] getReaderWriterInfo(Class<S> spiClass, SpiInfo spiInfo)
    {
        Iterator<S> iter;
        try {
            iter = theRegistry.getServiceProviders(spiClass, true);
        } catch (IllegalArgumentException e) {
            return new String[0];
        }
        HashSet<String> s = new HashSet<String>();
        while (iter.hasNext()) {
            ImageReaderWriterSpi spi = iter.next();
            Collections.addAll(s, spiInfo.info(spi));
        }
        return s.toArray(new String[s.size()]);
    }
    public static String[] getReaderFormatNames() {
        return getReaderWriterInfo(ImageReaderSpi.class,
                                   SpiInfo.FORMAT_NAMES);
    }
    public static String[] getReaderMIMETypes() {
        return getReaderWriterInfo(ImageReaderSpi.class,
                                   SpiInfo.MIME_TYPES);
    }
    public static String[] getReaderFileSuffixes() {
        return getReaderWriterInfo(ImageReaderSpi.class,
                                   SpiInfo.FILE_SUFFIXES);
    }
    static class ImageReaderIterator implements Iterator<ImageReader> {
        public Iterator iter;
        public ImageReaderIterator(Iterator iter) {
            this.iter = iter;
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public ImageReader next() {
            ImageReaderSpi spi = null;
            try {
                spi = (ImageReaderSpi)iter.next();
                return spi.createReaderInstance();
            } catch (IOException e) {
                theRegistry.deregisterServiceProvider(spi, ImageReaderSpi.class);
            }
            return null;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    static class CanDecodeInputFilter
        implements ServiceRegistry.Filter {
        Object input;
        public CanDecodeInputFilter(Object input) {
            this.input = input;
        }
        public boolean filter(Object elt) {
            try {
                ImageReaderSpi spi = (ImageReaderSpi)elt;
                ImageInputStream stream = null;
                if (input instanceof ImageInputStream) {
                    stream = (ImageInputStream)input;
                }
                boolean canDecode = false;
                if (stream != null) {
                    stream.mark();
                }
                canDecode = spi.canDecodeInput(input);
                if (stream != null) {
                    stream.reset();
                }
                return canDecode;
            } catch (IOException e) {
                return false;
            }
        }
    }
    static class CanEncodeImageAndFormatFilter
        implements ServiceRegistry.Filter {
        ImageTypeSpecifier type;
        String formatName;
        public CanEncodeImageAndFormatFilter(ImageTypeSpecifier type,
                                             String formatName) {
            this.type = type;
            this.formatName = formatName;
        }
        public boolean filter(Object elt) {
            ImageWriterSpi spi = (ImageWriterSpi)elt;
            return Arrays.asList(spi.getFormatNames()).contains(formatName) &&
                spi.canEncodeImage(type);
        }
    }
    static class ContainsFilter
        implements ServiceRegistry.Filter {
        Method method;
        String name;
        public ContainsFilter(Method method,
                              String name) {
            this.method = method;
            this.name = name;
        }
        public boolean filter(Object elt) {
            try {
                return contains((String[])method.invoke(elt), name);
            } catch (Exception e) {
                return false;
            }
        }
    }
    public static Iterator<ImageReader> getImageReaders(Object input) {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class,
                                              new CanDecodeInputFilter(input),
                                              true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }
    private static Method readerFormatNamesMethod;
    private static Method readerFileSuffixesMethod;
    private static Method readerMIMETypesMethod;
    private static Method writerFormatNamesMethod;
    private static Method writerFileSuffixesMethod;
    private static Method writerMIMETypesMethod;
    static {
        try {
            readerFormatNamesMethod =
                ImageReaderSpi.class.getMethod("getFormatNames");
            readerFileSuffixesMethod =
                ImageReaderSpi.class.getMethod("getFileSuffixes");
            readerMIMETypesMethod =
                ImageReaderSpi.class.getMethod("getMIMETypes");
            writerFormatNamesMethod =
                ImageWriterSpi.class.getMethod("getFormatNames");
            writerFileSuffixesMethod =
                ImageWriterSpi.class.getMethod("getFileSuffixes");
            writerMIMETypesMethod =
                ImageWriterSpi.class.getMethod("getMIMETypes");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public static Iterator<ImageReader>
        getImageReadersByFormatName(String formatName)
    {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class,
                                    new ContainsFilter(readerFormatNamesMethod,
                                                       formatName),
                                                true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }
    public static Iterator<ImageReader>
        getImageReadersBySuffix(String fileSuffix)
    {
        if (fileSuffix == null) {
            throw new IllegalArgumentException("fileSuffix == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class,
                                   new ContainsFilter(readerFileSuffixesMethod,
                                                      fileSuffix),
                                              true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }
    public static Iterator<ImageReader>
        getImageReadersByMIMEType(String MIMEType)
    {
        if (MIMEType == null) {
            throw new IllegalArgumentException("MIMEType == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class,
                                      new ContainsFilter(readerMIMETypesMethod,
                                                         MIMEType),
                                              true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }
    public static String[] getWriterFormatNames() {
        return getReaderWriterInfo(ImageWriterSpi.class,
                                   SpiInfo.FORMAT_NAMES);
    }
    public static String[] getWriterMIMETypes() {
        return getReaderWriterInfo(ImageWriterSpi.class,
                                   SpiInfo.MIME_TYPES);
    }
    public static String[] getWriterFileSuffixes() {
        return getReaderWriterInfo(ImageWriterSpi.class,
                                   SpiInfo.FILE_SUFFIXES);
    }
    static class ImageWriterIterator implements Iterator<ImageWriter> {
        public Iterator iter;
        public ImageWriterIterator(Iterator iter) {
            this.iter = iter;
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public ImageWriter next() {
            ImageWriterSpi spi = null;
            try {
                spi = (ImageWriterSpi)iter.next();
                return spi.createWriterInstance();
            } catch (IOException e) {
                theRegistry.deregisterServiceProvider(spi, ImageWriterSpi.class);
            }
            return null;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    private static boolean contains(String[] names, String name) {
        for (int i = 0; i < names.length; i++) {
            if (name.equalsIgnoreCase(names[i])) {
                return true;
            }
        }
        return false;
    }
    public static Iterator<ImageWriter>
        getImageWritersByFormatName(String formatName)
    {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class,
                                    new ContainsFilter(writerFormatNamesMethod,
                                                       formatName),
                                            true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }
    public static Iterator<ImageWriter>
        getImageWritersBySuffix(String fileSuffix)
    {
        if (fileSuffix == null) {
            throw new IllegalArgumentException("fileSuffix == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class,
                                   new ContainsFilter(writerFileSuffixesMethod,
                                                      fileSuffix),
                                            true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }
    public static Iterator<ImageWriter>
        getImageWritersByMIMEType(String MIMEType)
    {
        if (MIMEType == null) {
            throw new IllegalArgumentException("MIMEType == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class,
                                      new ContainsFilter(writerMIMETypesMethod,
                                                         MIMEType),
                                            true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }
    public static ImageWriter getImageWriter(ImageReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        ImageReaderSpi readerSpi = reader.getOriginatingProvider();
        if (readerSpi == null) {
            Iterator readerSpiIter;
            try {
                readerSpiIter =
                    theRegistry.getServiceProviders(ImageReaderSpi.class,
                                                    false);
            } catch (IllegalArgumentException e) {
                return null;
            }
            while (readerSpiIter.hasNext()) {
                ImageReaderSpi temp = (ImageReaderSpi) readerSpiIter.next();
                if (temp.isOwnReader(reader)) {
                    readerSpi = temp;
                    break;
                }
            }
            if (readerSpi == null) {
                return null;
            }
        }
        String[] writerNames = readerSpi.getImageWriterSpiNames();
        if (writerNames == null) {
            return null;
        }
        Class writerSpiClass = null;
        try {
            writerSpiClass = Class.forName(writerNames[0], true,
                                           ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
        ImageWriterSpi writerSpi = (ImageWriterSpi)
            theRegistry.getServiceProviderByClass(writerSpiClass);
        if (writerSpi == null) {
            return null;
        }
        try {
            return writerSpi.createWriterInstance();
        } catch (IOException e) {
            theRegistry.deregisterServiceProvider(writerSpi,
                                                  ImageWriterSpi.class);
            return null;
        }
    }
    public static ImageReader getImageReader(ImageWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer == null!");
        }
        ImageWriterSpi writerSpi = writer.getOriginatingProvider();
        if (writerSpi == null) {
            Iterator writerSpiIter;
            try {
                writerSpiIter =
                    theRegistry.getServiceProviders(ImageWriterSpi.class,
                                                    false);
            } catch (IllegalArgumentException e) {
                return null;
            }
            while (writerSpiIter.hasNext()) {
                ImageWriterSpi temp = (ImageWriterSpi) writerSpiIter.next();
                if (temp.isOwnWriter(writer)) {
                    writerSpi = temp;
                    break;
                }
            }
            if (writerSpi == null) {
                return null;
            }
        }
        String[] readerNames = writerSpi.getImageReaderSpiNames();
        if (readerNames == null) {
            return null;
        }
        Class readerSpiClass = null;
        try {
            readerSpiClass = Class.forName(readerNames[0], true,
                                           ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
        ImageReaderSpi readerSpi = (ImageReaderSpi)
            theRegistry.getServiceProviderByClass(readerSpiClass);
        if (readerSpi == null) {
            return null;
        }
        try {
            return readerSpi.createReaderInstance();
        } catch (IOException e) {
            theRegistry.deregisterServiceProvider(readerSpi,
                                                  ImageReaderSpi.class);
            return null;
        }
    }
    public static Iterator<ImageWriter>
        getImageWriters(ImageTypeSpecifier type, String formatName)
    {
        if (type == null) {
            throw new IllegalArgumentException("type == null!");
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class,
                                 new CanEncodeImageAndFormatFilter(type,
                                                                   formatName),
                                            true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }
    static class ImageTranscoderIterator
        implements Iterator<ImageTranscoder>
    {
        public Iterator iter;
        public ImageTranscoderIterator(Iterator iter) {
            this.iter = iter;
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public ImageTranscoder next() {
            ImageTranscoderSpi spi = null;
            spi = (ImageTranscoderSpi)iter.next();
            return spi.createTranscoderInstance();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    static class TranscoderFilter
        implements ServiceRegistry.Filter {
        String readerSpiName;
        String writerSpiName;
        public TranscoderFilter(ImageReaderSpi readerSpi,
                                ImageWriterSpi writerSpi) {
            this.readerSpiName = readerSpi.getClass().getName();
            this.writerSpiName = writerSpi.getClass().getName();
        }
        public boolean filter(Object elt) {
            ImageTranscoderSpi spi = (ImageTranscoderSpi)elt;
            String readerName = spi.getReaderServiceProviderName();
            String writerName = spi.getWriterServiceProviderName();
            return (readerName.equals(readerSpiName) &&
                    writerName.equals(writerSpiName));
        }
    }
    public static Iterator<ImageTranscoder>
        getImageTranscoders(ImageReader reader, ImageWriter writer)
    {
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer == null!");
        }
        ImageReaderSpi readerSpi = reader.getOriginatingProvider();
        ImageWriterSpi writerSpi = writer.getOriginatingProvider();
        ServiceRegistry.Filter filter =
            new TranscoderFilter(readerSpi, writerSpi);
        Iterator iter;
        try {
            iter = theRegistry.getServiceProviders(ImageTranscoderSpi.class,
                                            filter, true);
        } catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageTranscoderIterator(iter);
    }
    public static BufferedImage read(File input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        if (!input.canRead()) {
            throw new IIOException("Can't read input file!");
        }
        ImageInputStream stream = createImageInputStream(input);
        if (stream == null) {
            throw new IIOException("Can't create an ImageInputStream!");
        }
        BufferedImage bi = read(stream);
        if (bi == null) {
            stream.close();
        }
        return bi;
    }
    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        ImageInputStream stream = createImageInputStream(input);
        BufferedImage bi = read(stream);
        if (bi == null) {
            stream.close();
        }
        return bi;
    }
    public static BufferedImage read(URL input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        InputStream istream = null;
        try {
            istream = input.openStream();
        } catch (IOException e) {
            throw new IIOException("Can't get input stream from URL!", e);
        }
        ImageInputStream stream = createImageInputStream(istream);
        BufferedImage bi;
        try {
            bi = read(stream);
            if (bi == null) {
                stream.close();
            }
        } finally {
            istream.close();
        }
        return bi;
    }
    public static BufferedImage read(ImageInputStream stream)
        throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        Iterator iter = getImageReaders(stream);
        if (!iter.hasNext()) {
            return null;
        }
        ImageReader reader = (ImageReader)iter.next();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);
        BufferedImage bi;
        try {
            bi = reader.read(0, param);
        } finally {
            reader.dispose();
            stream.close();
        }
        return bi;
    }
    public static boolean write(RenderedImage im,
                                String formatName,
                                ImageOutputStream output) throws IOException {
        if (im == null) {
            throw new IllegalArgumentException("im == null!");
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        return doWrite(im, getWriter(im, formatName), output);
    }
    public static boolean write(RenderedImage im,
                                String formatName,
                                File output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        ImageOutputStream stream = null;
        ImageWriter writer = getWriter(im, formatName);
        if (writer == null) {
            return false;
        }
        try {
            output.delete();
            stream = createImageOutputStream(output);
        } catch (IOException e) {
            throw new IIOException("Can't create output stream!", e);
        }
        try {
            return doWrite(im, writer, stream);
        } finally {
            stream.close();
        }
    }
    public static boolean write(RenderedImage im,
                                String formatName,
                                OutputStream output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        ImageOutputStream stream = null;
        try {
            stream = createImageOutputStream(output);
        } catch (IOException e) {
            throw new IIOException("Can't create output stream!", e);
        }
        try {
            return doWrite(im, getWriter(im, formatName), stream);
        } finally {
            stream.close();
        }
    }
    private static ImageWriter getWriter(RenderedImage im,
                                         String formatName) {
        ImageTypeSpecifier type =
            ImageTypeSpecifier.createFromRenderedImage(im);
        Iterator<ImageWriter> iter = getImageWriters(type, formatName);
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }
    private static boolean doWrite(RenderedImage im, ImageWriter writer,
                                 ImageOutputStream output) throws IOException {
        if (writer == null) {
            return false;
        }
        writer.setOutput(output);
        try {
            writer.write(im);
        } finally {
            writer.dispose();
            output.flush();
        }
        return true;
    }
}
