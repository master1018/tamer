class EffectUtils {
    static void clearImage(BufferedImage img) {
        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2.dispose();
    }
    static BufferedImage gaussianBlur(BufferedImage src, BufferedImage dst, int radius) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null || dst.getWidth() != width || dst.getHeight() != height || src.getType() != dst.getType()) {
            dst = createColorModelCompatibleImage(src);
        }
        float[] kernel = createGaussianKernel(radius);
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            int[] srcPixels = new int[width * height];
            int[] dstPixels = new int[width * height];
            getPixels(src, 0, 0, width, height, srcPixels);
            blur(srcPixels, dstPixels, width, height, kernel, radius);
            blur(dstPixels, srcPixels, height, width, kernel, radius);
            setPixels(dst, 0, 0, width, height, srcPixels);
        } else if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            byte[] srcPixels = new byte[width * height];
            byte[] dstPixels = new byte[width * height];
            getPixels(src, 0, 0, width, height, srcPixels);
            blur(srcPixels, dstPixels, width, height, kernel, radius);
            blur(dstPixels, srcPixels, height, width, kernel, radius);
            setPixels(dst, 0, 0, width, height, srcPixels);
        } else {
            throw new IllegalArgumentException("EffectUtils.gaussianBlur() src image is not a supported type, type=[" +
                    src.getType() + "]");
        }
        return dst;
    }
    private static void blur(int[] srcPixels, int[] dstPixels,
                             int width, int height,
                             float[] kernel, int radius) {
        float a;
        float r;
        float g;
        float b;
        int ca;
        int cr;
        int cg;
        int cb;
        for (int y = 0; y < height; y++) {
            int index = y;
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                a = r = g = b = 0.0f;
                for (int i = -radius; i <= radius; i++) {
                    int subOffset = x + i;
                    if (subOffset < 0 || subOffset >= width) {
                        subOffset = (x + width) % width;
                    }
                    int pixel = srcPixels[offset + subOffset];
                    float blurFactor = kernel[radius + i];
                    a += blurFactor * ((pixel >> 24) & 0xFF);
                    r += blurFactor * ((pixel >> 16) & 0xFF);
                    g += blurFactor * ((pixel >> 8) & 0xFF);
                    b += blurFactor * ((pixel) & 0xFF);
                }
                ca = (int) (a + 0.5f);
                cr = (int) (r + 0.5f);
                cg = (int) (g + 0.5f);
                cb = (int) (b + 0.5f);
                dstPixels[index] = ((ca > 255 ? 255 : ca) << 24) |
                        ((cr > 255 ? 255 : cr) << 16) |
                        ((cg > 255 ? 255 : cg) << 8) |
                        (cb > 255 ? 255 : cb);
                index += height;
            }
        }
    }
    static void blur(byte[] srcPixels, byte[] dstPixels,
                            int width, int height,
                            float[] kernel, int radius) {
        float p;
        int cp;
        for (int y = 0; y < height; y++) {
            int index = y;
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                p = 0.0f;
                for (int i = -radius; i <= radius; i++) {
                    int subOffset = x + i;
                    if (subOffset < 0 || subOffset >= width) {
                        subOffset = (x + width) % width;
                    }
                    int pixel = srcPixels[offset + subOffset] & 0xFF;
                    float blurFactor = kernel[radius + i];
                    p += blurFactor * pixel;
                }
                cp = (int) (p + 0.5f);
                dstPixels[index] = (byte) (cp > 255 ? 255 : cp);
                index += height;
            }
        }
    }
    static float[] createGaussianKernel(int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }
        float[] data = new float[radius * 2 + 1];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }
        return data;
    }
    static byte[] getPixels(BufferedImage img,
                                   int x, int y, int w, int h, byte[] pixels) {
        if (w == 0 || h == 0) {
            return new byte[0];
        }
        if (pixels == null) {
            pixels = new byte[w * h];
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
            Raster raster = img.getRaster();
            return (byte[]) raster.getDataElements(x, y, w, h, pixels);
        } else {
            throw new IllegalArgumentException("Only type BYTE_GRAY is supported");
        }
    }
    static void setPixels(BufferedImage img,
                                 int x, int y, int w, int h, byte[] pixels) {
        if (pixels == null || w == 0 || h == 0) {
            return;
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
            WritableRaster raster = img.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            throw new IllegalArgumentException("Only type BYTE_GRAY is supported");
        }
    }
    public static int[] getPixels(BufferedImage img,
                                  int x, int y, int w, int h, int[] pixels) {
        if (w == 0 || h == 0) {
            return new int[0];
        }
        if (pixels == null) {
            pixels = new int[w * h];
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length" +
                                               " >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB) {
            Raster raster = img.getRaster();
            return (int[]) raster.getDataElements(x, y, w, h, pixels);
        }
        return img.getRGB(x, y, w, h, pixels, 0, w);
    }
    public static void setPixels(BufferedImage img,
                                 int x, int y, int w, int h, int[] pixels) {
        if (pixels == null || w == 0 || h == 0) {
            return;
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length" +
                                               " >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB) {
            WritableRaster raster = img.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            img.setRGB(x, y, w, h, pixels, 0, w);
        }
    }
    public static BufferedImage createColorModelCompatibleImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        return new BufferedImage(cm,
            cm.createCompatibleWritableRaster(image.getWidth(),
                                              image.getHeight()),
            cm.isAlphaPremultiplied(), null);
    }
    public static BufferedImage createCompatibleTranslucentImage(int width,
                                                                 int height) {
        return isHeadless() ?
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) :
                getGraphicsConfiguration().createCompatibleImage(width, height,
                                                   Transparency.TRANSLUCENT);
    }
    private static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }
    private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();
    }
}
