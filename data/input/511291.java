public class TrackingPatternView extends View {
    private Bitmap mTexture;
    private Paint mPaint;
    private int mTextureWidth;
    private int mTextureHeight;
    public TrackingPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTexture = BitmapFactory.decodeResource(getResources(), 
                com.android.internal.R.drawable.status_bar_background);
        mTextureWidth = mTexture.getWidth();
        mTextureHeight = mTexture.getHeight();
        mPaint = new Paint();
        mPaint.setDither(false);
    }
    @Override
    public void onDraw(Canvas canvas) {
        final Bitmap texture = mTexture;
        final Paint paint = mPaint;
        final int width = getWidth();
        final int height = getHeight();
        final int textureWidth = mTextureWidth;
        final int textureHeight = mTextureHeight;
        int x = 0;
        int y;
        while (x < width) {
            y = 0;
            while (y < height) {
                canvas.drawBitmap(texture, x, y, paint);
                y += textureHeight;
            }
            x += textureWidth;
        }
    }
}
