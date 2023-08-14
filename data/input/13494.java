public class BooleanAttributes {
    private static TransformerFactory transformerFactory =
        TransformerFactory.newInstance();
    private static XPath xpathEngine = XPathFactory.newInstance().newXPath();
    public static void main(String[] args) throws Exception {
        test("image/png", false, "<javax_imageio_1.0 />",
             "Chroma/BlackIsZero/@value",
             "Compression/Lossless/@value");
        test("image/png", false,
             "<javax_imageio_png_1.0>" +
             "<iTXt><iTXtEntry keyword='Comment' compressionFlag='TRUE' " +
             "compressionMethod='0' languageTag='en' " +
             "translatedKeyword='comment' text='foo'/></iTXt>" +
             "</javax_imageio_png_1.0>",
             "iTXt/iTXtEntry/@compressionFlag");
        test("image/png", false,
             "<javax_imageio_png_1.0>" +
             "<iTXt><iTXtEntry keyword='Comment' compressionFlag='FALSE' " +
             "compressionMethod='0' languageTag='en' " +
             "translatedKeyword='comment' text='foo'/></iTXt>" +
             "</javax_imageio_png_1.0>",
             "iTXt/iTXtEntry/@compressionFlag");
        test("image/gif", false, "<javax_imageio_1.0 />",
             "Chroma/BlackIsZero/@value",
             "Compression/Lossless/@value");
        test("image/gif", false,
             "<javax_imageio_gif_image_1.0>" +
             "<ImageDescriptor imageLeftPosition='0' imageTopPosition='0' " +
             "imageWidth='16' imageHeight='16' interlaceFlag='TRUE' />" +
             "<LocalColorTable sizeOfLocalColorTable='2' " +
             "backgroundColorIndex='1' sortFlag='TRUE'>" +
             "<ColorTableEntry index='0' red='0' green='0' blue='0' />" +
             "<ColorTableEntry index='1' red='255' green='255' blue='255' />" +
             "</LocalColorTable>" +
             "<GraphicControlExtension disposalMethod='doNotDispose' " +
             "userInputFlag='FALSE' transparentColorFlag='TRUE' " +
             "delayTime='100' transparentColorIndex='1' />" +
             "</javax_imageio_gif_image_1.0>",
             "ImageDescriptor/@interlaceFlag",
             "LocalColorTable/@sortFlag",
             "GraphicControlExtension/@userInputFlag",
             "GraphicControlExtension/@transparentColorFlag");
        test("image/gif", true,
             "<javax_imageio_gif_stream_1.0>" +
             "<GlobalColorTable sizeOfGlobalColorTable='2' " +
             "backgroundColorIndex='1' sortFlag='TRUE'>" +
             "<ColorTableEntry index='0' red='0' green='0' blue='0' />" +
             "<ColorTableEntry index='1' red='255' green='255' blue='255' />" +
             "</GlobalColorTable>" +
             "</javax_imageio_gif_stream_1.0>",
             "GlobalColorTable/@sortFlag");
        test("image/jpeg", false, "<javax_imageio_1.0 />",
             "Compression/Lossless/@value");
    }
    private static void transform(Source src, Result dst)
        throws Exception
    {
        transformerFactory.newTransformer().transform(src, dst);
    }
    private static void verify(Node meta, String[] xpaths, boolean required)
        throws Exception
    {
        for (String xpath: xpaths) {
            NodeList list = (NodeList)
                xpathEngine.evaluate(xpath, meta, XPathConstants.NODESET);
            if (list.getLength() == 0 && required)
                throw new AssertionError("Missing value: " + xpath);
            for (int i = 0; i < list.getLength(); ++i) {
                String value = list.item(i).getNodeValue();
                if (!(value.equals("TRUE") || value.equals("FALSE")))
                    throw new AssertionError(xpath + " has value " + value);
            }
        }
    }
    public static void test(String mimeType, boolean useStreamMeta,
                            String metaXml, String... boolXpaths)
        throws Exception
    {
        BufferedImage img =
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ImageWriter iw = ImageIO.getImageWritersByMIMEType(mimeType).next();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(os);
        iw.setOutput(ios);
        ImageWriteParam param = null;
        IIOMetadata streamMeta = iw.getDefaultStreamMetadata(param);
        IIOMetadata imageMeta =
            iw.getDefaultImageMetadata(new ImageTypeSpecifier(img), param);
        IIOMetadata meta = useStreamMeta ? streamMeta : imageMeta;
        Source src = new StreamSource(new StringReader(metaXml));
        DOMResult dst = new DOMResult();
        transform(src, dst);
        Document doc = (Document)dst.getNode();
        Element node = doc.getDocumentElement();
        String metaFormat = node.getNodeName();
        verify(meta.getAsTree(metaFormat), boolXpaths, false);
        meta.mergeTree(metaFormat, node);
        verify(meta.getAsTree(metaFormat), boolXpaths, true);
        iw.write(streamMeta, new IIOImage(img, null, imageMeta), param);
        iw.dispose();
        ios.close();
        ImageReader ir = ImageIO.getImageReader(iw);
        byte[] bytes = os.toByteArray();
        if (bytes.length == 0)
            throw new AssertionError("Zero length image file");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        ImageInputStream iis = new MemoryCacheImageInputStream(is);
        ir.setInput(iis);
        if (useStreamMeta) meta = ir.getStreamMetadata();
        else meta = ir.getImageMetadata(0);
        verify(meta.getAsTree(metaFormat), boolXpaths, true);
    }
    public static void xtest(Object... eatAnyArguments) {
        System.err.println("Disabled test! Change xtest back into test!");
    }
}
