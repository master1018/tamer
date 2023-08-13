public class PngOutputTypeTest {
    public static void main(String[] args) throws IOException {
        new PngOutputTypeTest(BufferedImage.TYPE_INT_RGB).doTest();
        new PngOutputTypeTest(BufferedImage.TYPE_INT_ARGB).doTest();
    }
    ImageInputStream iis;
    ImageReader reader;
    public PngOutputTypeTest(int type) throws IOException {
        this(createTestImage(type));
    }
    public PngOutputTypeTest(File f) throws IOException {
        this(ImageIO.createImageInputStream(f));
    }
    public PngOutputTypeTest(ImageInputStream iis) throws IOException {
        this.iis = iis;
        reader = ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);
    }
    BufferedImage def;
    BufferedImage raw;
    ImageTypeSpecifier raw_type;
    public void doTest() throws IOException {
        if (!checkImageType()) {
            System.out.println("Test IGNORED!");
            return;
        }
        def = reader.read(0);
        System.out.println("Default image type: " + def.getType());
        if (def == null || def.getType() == BufferedImage.TYPE_CUSTOM) {
            throw new RuntimeException("Test FAILED!");
        }
        raw_type = reader.getRawImageType(0);
        ImageReadParam param = reader.getDefaultReadParam();
        param.setDestinationType(raw_type);
        System.out.println("Reading with raw image type...");
        raw = reader.read(0, param);
        System.out.println("Type of raw image is " + raw.getType());
        compare(def, raw);
        Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
        while (types.hasNext()) {
            ImageTypeSpecifier t = types.next();
            System.out.println("Test type: " + t);
            param.setDestinationType(t);
            BufferedImage img = reader.read(0, param);
            System.out.println("Result type: " + img.getType());
            compare(def, img);
            System.out.println("Done.\n");
        }
        System.out.println("Test PASSED.");
    }
    private boolean checkImageType() throws IOException {
        IIOMetadata md  = null;
        try {
            md = reader.getImageMetadata(0);
        } catch (IOException e) {
            return false;
        }
        String format = md.getNativeMetadataFormatName();
        Node root = md.getAsTree(format);
        Node ihdr = getNode(root, "IHDR");
        if (ihdr == null) {
            throw new RuntimeException("No ihdr node: invalid png image!");
        }
        String colorType = getAttributeValue(ihdr, "colorType");
        System.out.println("ColorType: " + colorType);
        if ("RGB".equals(colorType) || "RGBAlpha".equals(colorType)) {
            System.out.println("Good color type!");
            String bitDepthStr = getAttributeValue(ihdr, "bitDepth");
            System.out.println("bitDepth: " + bitDepthStr);
            int bitDepth = -1;
            try {
                bitDepth = Integer.parseInt(bitDepthStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid bitDepth!");
            }
            if (bitDepth == 8) {
                return true;
            }
        }
        return false;
    }
    private String getAttributeValue(Node n, String attrname) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs == null) {
            return null;
        } else {
            Node a = attrs.getNamedItem(attrname);
            if (a == null) {
                return null;
            } else {
                return a.getNodeValue();
            }
        }
    }
    private Node getNode(Node root, String name) {
        Node n = root;
        return lookupNode(n, name);
    }
    private Node lookupNode(Node n, String name) {
        if (n == null) {
            return null;
        }
        if (name.equals(n.getNodeName())) {
            return n;
        } else {
            Node res = lookupNode(n.getNextSibling(), name);
            if (res != null) {
                return res;
            } else {
                return lookupNode(n.getFirstChild(), name);
            }
        }
    }
    private static void compare(BufferedImage a, BufferedImage b) {
        int w = a.getWidth();
        int h = a.getHeight();
        if (w != b.getWidth() || h != b.getHeight()) {
            throw new RuntimeException("Test FAILED!");
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    throw new RuntimeException("Test FAILED!");
                }
            }
        }
    }
    static Color[] colors = new Color[] { Color.red, Color.green, Color.blue };
    private static ImageInputStream createTestImage(int type) throws IOException  {
        int w = 100;
        int h = 100;
        BufferedImage img = new BufferedImage(w, h, type);
        int dx = w / colors.length;
        for (int i = 0; i < colors.length; i++) {
            for (int x = i *dx; (x < (i + 1) * dx) && (x < w) ; x++) {
                for (int y = 0; y < h; y++) {
                    img.setRGB(x, y, colors[i].getRGB());
                }
            }
        }
        File pwd = new File(".");
        File out = File.createTempFile("rgba_", ".png", pwd);
        System.out.println("Create file: " + out.getAbsolutePath());
        ImageIO.write(img, "PNG", out);
        return ImageIO.createImageInputStream(out);
    }
}
