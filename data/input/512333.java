public final class AdaptiveBackgroundTexture extends Texture {
    private static final int RED_MASK = 0xff0000;
    private static final int RED_MASK_SHIFT = 16;
    private static final int GREEN_MASK = 0x00ff00;
    private static final int GREEN_MASK_SHIFT = 8;
    private static final int BLUE_MASK = 0x0000ff;
    private static final int RADIUS = 4;
    private static final int KERNEL_SIZE = RADIUS * 2 + 1;
    private static final int NUM_COLORS = 256;
    private static final int MAX_COLOR_VALUE = NUM_COLORS - 1;
    private static final int[] KERNEL_NORM = new int[KERNEL_SIZE * NUM_COLORS];
    private static final int MULTIPLY_COLOR = 0xffaaaaaa;
    private static final int START_FADE_X = 96;
    private static final int THUMBNAIL_MAX_X = 128;
    private final int mWidth;
    private final int mHeight;
    private final Bitmap mSource;
    private Texture mBaseTexture;
    static {
        for (int i = KERNEL_SIZE * NUM_COLORS - 1; i >= 0; --i) {
            KERNEL_NORM[i] = i / KERNEL_SIZE;
        }
    }
    public AdaptiveBackgroundTexture(Bitmap source, int width, int height) {
        mSource = source;
        mWidth = width;
        mHeight = height;
        mBaseTexture = null;
    }
    public AdaptiveBackgroundTexture(Texture texture, int width, int height) {
        mBaseTexture = texture;
        mSource = null;
        mWidth = width;
        mHeight = height;
    }
    @Override
    protected boolean shouldQueue() {
        return true;
    }
    @Override
    public boolean isCached() {
        return true;
    }
    @Override
    protected Bitmap load(RenderView view) {
        Bitmap source = mSource;
        if (source == null) {
            if (mBaseTexture != null) {
                source = mBaseTexture.load(view);
                if (source == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        source = Utils.resizeBitmap(source, THUMBNAIL_MAX_X);
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int destWidth = mWidth;
        int destHeight = mHeight;
        float fitX = (float) sourceWidth / (float) destWidth;
        float fitY = (float) sourceHeight / (float) destHeight;
        float scale;
        int cropX;
        int cropY;
        int cropWidth;
        int cropHeight;
        if (fitX < fitY) {
            cropWidth = sourceWidth;
            cropHeight = (int) (destHeight * fitX);
            cropX = 0;
            cropY = (sourceHeight - cropHeight) / 2;
            scale = 1.0f / fitX;
        } else {
            cropWidth = (int) (destHeight * fitY);
            cropHeight = sourceHeight;
            cropX = (sourceWidth - cropWidth) / 2;
            cropY = 0;
            scale = 1f / fitY;
        }
        int numPixels = cropWidth * cropHeight;
        int[] in = new int[numPixels];
        int[] tmp = new int[numPixels];
        source.getPixels(in, 0, cropWidth, cropX, cropY, cropWidth, cropHeight);
        boxBlurFilter(in, tmp, cropWidth, cropHeight, cropWidth);
        boxBlurFilter(tmp, in, cropHeight, cropWidth, START_FADE_X);
        Bitmap filtered = Bitmap.createBitmap(in, cropWidth, cropHeight, Bitmap.Config.ARGB_8888);
        Bitmap output = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColorFilter(new LightingColorFilter(MULTIPLY_COLOR, 0));
        canvas.scale(scale, scale);
        canvas.drawBitmap(filtered, 0f, 0f, paint);
        filtered.recycle();
        mBaseTexture = null;
        return output;
    }
    private static void boxBlurFilter(int[] in, int[] out, int width, int height, int startFadeX) {
        int inPos = 0;
        int maxX = width - 1;
        for (int y = 0; y < height; ++y) {
            int red = 0;
            int green = 0;
            int blue = 0;
            for (int i = -RADIUS; i <= RADIUS; ++i) {
                int argb = in[inPos + FloatUtils.clamp(i, 0, maxX)];
                red += (argb & RED_MASK) >> RED_MASK_SHIFT;
                green += (argb & GREEN_MASK) >> GREEN_MASK_SHIFT;
                blue += argb & BLUE_MASK;
            }
            int alpha = (y < startFadeX) ? 0xff : ((height - y - 1) * MAX_COLOR_VALUE / (height - startFadeX));
            int outPos = y;
            for (int x = 0; x != width; ++x) { 
                out[outPos] = (alpha << 24) | (KERNEL_NORM[red] << RED_MASK_SHIFT) | (KERNEL_NORM[green] << GREEN_MASK_SHIFT)
                        | KERNEL_NORM[blue];
                int prevX = FloatUtils.clamp(x - RADIUS, 0, maxX);
                int nextX = FloatUtils.clamp(x + RADIUS + 1, 0, maxX);
                int prevArgb = in[inPos + prevX];
                int nextArgb = in[inPos + nextX];
                red += ((nextArgb & RED_MASK) - (prevArgb & RED_MASK)) >> RED_MASK_SHIFT;
                green += ((nextArgb & GREEN_MASK) - (prevArgb & GREEN_MASK)) >> GREEN_MASK_SHIFT;
                blue += (nextArgb & BLUE_MASK) - (prevArgb & BLUE_MASK);
                outPos += height;
            }
            inPos += width;
        }
    }
}
