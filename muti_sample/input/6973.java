abstract class OutputImageTests extends OutputTests {
    private static final int TEST_IMAGEIO     = 1;
    private static final int TEST_IMAGEWRITER = 2;
    private static Group imageRoot;
    private static Group imageioRoot;
    private static Group imageioOptRoot;
    private static ImageWriterSpi[] imageioWriterSpis;
    private static String[] imageioWriteFormatShortNames;
    private static Option imageioWriteFormatList;
    private static Group imageioTestRoot;
    private static Group imageWriterRoot;
    private static Group imageWriterOptRoot;
    private static Option installListenerTog;
    private static Group imageWriterTestRoot;
    public static void init() {
        imageRoot = new Group(outputRoot, "image", "Image Writing Benchmarks");
        imageRoot.setTabbed();
        if (hasImageIO) {
            imageioRoot = new Group(imageRoot, "imageio", "Image I/O");
            imageioOptRoot = new Group(imageioRoot, "opts",
                                       "Image I/O Options");
            initIIOWriteFormats();
            imageioWriteFormatList =
                new Option.ObjectList(imageioOptRoot,
                                      "format", "Image Format",
                                      imageioWriteFormatShortNames,
                                      imageioWriterSpis,
                                      imageioWriteFormatShortNames,
                                      imageioWriteFormatShortNames,
                                      0x0);
            imageioTestRoot = new Group(imageioRoot, "tests",
                                        "Image I/O Tests");
            new ImageIOWrite();
            imageWriterRoot = new Group(imageioRoot, "writer",
                                        "ImageWriter Benchmarks");
            imageWriterOptRoot = new Group(imageWriterRoot, "opts",
                                           "ImageWriter Options");
            installListenerTog =
                new Option.Toggle(imageWriterOptRoot,
                                  "installListener",
                                  "Install Progress Listener",
                                  Option.Toggle.Off);
            imageWriterTestRoot = new Group(imageWriterRoot, "tests",
                                            "ImageWriter Tests");
            new ImageWriterWrite();
        }
    }
    private static void initIIOWriteFormats() {
        List spis = new ArrayList();
        List shortNames = new ArrayList();
        ImageIO.scanForPlugins();
        IIORegistry registry = IIORegistry.getDefaultInstance();
        java.util.Iterator writerspis =
            registry.getServiceProviders(ImageWriterSpi.class, false);
        while (writerspis.hasNext()) {
            ImageWriterSpi spi = (ImageWriterSpi)writerspis.next();
            String klass = spi.getClass().getName();
            String format = spi.getFormatNames()[0].toLowerCase();
            String suffix = spi.getFileSuffixes()[0].toLowerCase();
            if (suffix == null || suffix.equals("")) {
                suffix = format;
            }
            String shortName;
            if (klass.startsWith("com.sun.imageio.plugins")) {
                shortName = "core-" + suffix;
            } else {
                shortName = "ext-" + suffix;
            }
            spis.add(spi);
            shortNames.add(shortName);
        }
        imageioWriterSpis = new ImageWriterSpi[spis.size()];
        imageioWriterSpis = (ImageWriterSpi[])spis.toArray(imageioWriterSpis);
        imageioWriteFormatShortNames = new String[shortNames.size()];
        imageioWriteFormatShortNames =
            (String[])shortNames.toArray(imageioWriteFormatShortNames);
    }
    protected OutputImageTests(Group parent,
                               String nodeName, String description)
    {
        super(parent, nodeName, description);
    }
    public void cleanupTest(TestEnvironment env, Object ctx) {
        Context iioctx = (Context)ctx;
        iioctx.cleanup(env);
    }
    private static class Context extends OutputTests.Context {
        String format;
        BufferedImage image;
        ImageWriter writer;
        Context(TestEnvironment env, Result result, int testType) {
            super(env, result);
            String content = (String)env.getModifier(contentList);
            if (content == null) {
                content = CONTENT_BLANK;
            }
            image = createBufferedImage(size, size, content, false);
            result.setUnits(size*size);
            result.setUnitName("pixel");
            if (testType == TEST_IMAGEIO || testType == TEST_IMAGEWRITER) {
                ImageWriterSpi writerspi =
                    (ImageWriterSpi)env.getModifier(imageioWriteFormatList);
                format = writerspi.getFileSuffixes()[0].toLowerCase();
                if (testType == TEST_IMAGEWRITER) {
                    try {
                        writer = writerspi.createWriterInstance();
                    } catch (IOException e) {
                        System.err.println("error creating writer");
                        e.printStackTrace();
                    }
                    if (env.isEnabled(installListenerTog)) {
                        writer.addIIOWriteProgressListener(
                            new WriteProgressListener());
                    }
                }
                if (format.equals("wbmp")) {
                    BufferedImage newimg =
                        new BufferedImage(size, size,
                                          BufferedImage.TYPE_BYTE_BINARY);
                    Graphics g = newimg.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                    image = newimg;
                }
            } else { 
                format = "jpeg";
            }
            initOutput();
        }
        void initContents(File f) throws IOException {
            ImageIO.write(image, format, f);
        }
        void initContents(OutputStream out) throws IOException {
            ImageIO.write(image, format, out);
        }
        void cleanup(TestEnvironment env) {
            super.cleanup(env);
            if (writer != null) {
                writer.dispose();
                writer = null;
            }
        }
    }
    private static class ImageIOWrite extends OutputImageTests {
        public ImageIOWrite() {
            super(imageioTestRoot,
                  "imageioWrite",
                  "ImageIO.write()");
            addDependency(generalDestRoot,
                new Modifier.Filter() {
                    public boolean isCompatible(Object val) {
                        OutputType t = (OutputType)val;
                        return (t.getType() != OUTPUT_FILECHANNEL);
                    }
                });
            addDependencies(imageioOptRoot, true);
        }
        public Object initTest(TestEnvironment env, Result result) {
            return new Context(env, result, TEST_IMAGEIO);
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final Object output = ictx.output;
            final BufferedImage image = ictx.image;
            final String format = ictx.format;
            final int outputType = ictx.outputType;
            switch (outputType) {
            case OUTPUT_FILE:
                do {
                    try {
                        ImageIO.write(image, format, (File)output);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (--numReps >= 0);
                break;
            case OUTPUT_ARRAY:
                do {
                    try {
                        ByteArrayOutputStream baos =
                            new ByteArrayOutputStream();
                        BufferedOutputStream bos =
                            new BufferedOutputStream(baos);
                        ImageIO.write(image, format, bos);
                        baos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (--numReps >= 0);
                break;
            default:
                throw new IllegalArgumentException("Invalid output type");
            }
        }
    }
    private static class ImageWriterWrite extends OutputImageTests {
        public ImageWriterWrite() {
            super(imageWriterTestRoot,
                  "write",
                  "ImageWriter.write()");
            addDependency(generalDestRoot);
            addDependencies(imageioGeneralOptRoot, true);
            addDependencies(imageioOptRoot, true);
            addDependencies(imageWriterOptRoot, true);
        }
        public Object initTest(TestEnvironment env, Result result) {
            return new Context(env, result, TEST_IMAGEWRITER);
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageWriter writer = ictx.writer;
            final BufferedImage image = ictx.image;
            do {
                try {
                    ImageOutputStream ios = ictx.createImageOutputStream();
                    writer.setOutput(ios);
                    writer.write(image);
                    writer.reset();
                    ios.close();
                    ictx.closeOriginalStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (--numReps >= 0);
        }
    }
    private static class WriteProgressListener
        implements IIOWriteProgressListener
    {
        public void imageStarted(ImageWriter source, int imageIndex) {}
        public void imageProgress(ImageWriter source,
                                  float percentageDone) {}
        public void imageComplete(ImageWriter source) {}
        public void thumbnailStarted(ImageWriter source,
                                     int imageIndex, int thumbnailIndex) {}
        public void thumbnailProgress(ImageWriter source,
                                      float percentageDone) {}
        public void thumbnailComplete(ImageWriter source) {}
        public void writeAborted(ImageWriter source) {}
    }
}
