class XIconInfo {
    private int[] intIconData;
    private long[] longIconData;
    private Image image;
    private final int width;
    private final int height;
    private int scaledWidth;
    private int scaledHeight;
    private int rawLength;
    XIconInfo(int[] intIconData) {
        this.intIconData =
            (null == intIconData) ? null : Arrays.copyOf(intIconData, intIconData.length);
        this.width = intIconData[0];
        this.height = intIconData[1];
        this.scaledWidth = width;
        this.scaledHeight = height;
        this.rawLength = width * height + 2;
    }
    XIconInfo(long[] longIconData) {
        this.longIconData =
        (null == longIconData) ? null : Arrays.copyOf(longIconData, longIconData.length);
        this.width = (int)longIconData[0];
        this.height = (int)longIconData[1];
        this.scaledWidth = width;
        this.scaledHeight = height;
        this.rawLength = width * height + 2;
    }
    XIconInfo(Image image) {
        this.image = image;
        if (image instanceof ToolkitImage) {
            ImageRepresentation ir = ((ToolkitImage)image).getImageRep();
            ir.reconstruct(ImageObserver.ALLBITS);
            this.width = ir.getWidth();
            this.height = ir.getHeight();
        } else {
            this.width = image.getWidth(null);
            this.height = image.getHeight(null);
        }
        this.scaledWidth = width;
        this.scaledHeight = height;
        this.rawLength = width * height + 2;
    }
    void setScaledSize(int width, int height) {
        this.scaledWidth = width;
        this.scaledHeight = height;
        this.rawLength = width * height + 2;
    }
    boolean isValid() {
        return (width > 0 && height > 0);
    }
    int getWidth() {
        return width;
    }
    int getHeight() {
        return height;
    }
    public String toString() {
        return "XIconInfo[w=" + width + ",h=" + height + ",sw=" + scaledWidth + ",sh=" + scaledHeight + "]";
    }
    int getRawLength() {
        return rawLength;
    }
    int[] getIntData() {
        if (this.intIconData == null) {
            if (this.longIconData != null) {
                this.intIconData = longArrayToIntArray(longIconData);
            } else if (this.image != null) {
                this.intIconData = imageToIntArray(this.image, scaledWidth, scaledHeight);
            }
        }
        return this.intIconData;
    }
    long[] getLongData() {
        if (this.longIconData == null) {
            if (this.intIconData != null) {
                this.longIconData = intArrayToLongArray(this.intIconData);
            } else if (this.image != null) {
                int[] intIconData = imageToIntArray(this.image, scaledWidth, scaledHeight);
                this.longIconData = intArrayToLongArray(intIconData);
            }
        }
        return this.longIconData;
    }
    Image getImage() {
        if (this.image == null) {
            if (this.intIconData != null) {
                this.image = intArrayToImage(this.intIconData);
            } else if (this.longIconData != null) {
                int[] intIconData = longArrayToIntArray(this.longIconData);
                this.image = intArrayToImage(intIconData);
            }
        }
        return this.image;
    }
    private static int[] longArrayToIntArray(long[] longData) {
        int[] intData = new int[longData.length];
        for (int i = 0; i < longData.length; i++) {
            intData[i] = (int)longData[i];
        }
        return intData;
    }
    private static long[] intArrayToLongArray(int[] intData) {
        long[] longData = new long[intData.length];
        for (int i = 0; i < intData.length; i++) {
            longData[i] = (int)intData[i];
        }
        return longData;
    }
    static Image intArrayToImage(int[] raw) {
        ColorModel cm =
            new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32,
                                 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000,
                                 false, DataBuffer.TYPE_INT);
        DataBuffer buffer = new DataBufferInt(raw, raw.length-2, 2);
        WritableRaster raster =
            Raster.createPackedRaster(buffer, raw[0], raw[1],
                                      raw[0],
                                      new int[] {0x00ff0000, 0x0000ff00,
                                                 0x000000ff, 0xff000000},
                                      null);
        BufferedImage im = new BufferedImage(cm, raster, false, null);
        return im;
    }
    static int[] imageToIntArray(Image image, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        ColorModel cm =
            new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32,
                                 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000,
                                 false, DataBuffer.TYPE_INT);
        DataBufferInt buffer = new DataBufferInt(width * height);
        WritableRaster raster =
            Raster.createPackedRaster(buffer, width, height,
                                      width,
                                      new int[] {0x00ff0000, 0x0000ff00,
                                                 0x000000ff, 0xff000000},
                                      null);
        BufferedImage im = new BufferedImage(cm, raster, false, null);
        Graphics g = im.getGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        int[] data = buffer.getData();
        int[] raw = new int[width * height + 2];
        raw[0] = width;
        raw[1] = height;
        System.arraycopy(data, 0, raw, 2, width * height);
        return raw;
    }
}
