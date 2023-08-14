public class Plasma extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new PlasmaView(this));
    }
    static {
        System.loadLibrary("plasma");
    }
}
class PlasmaView extends View {
    private Bitmap mBitmap;
    private long mStartTime;
    private static native void renderPlasma(Bitmap  bitmap, long time_ms);
    public PlasmaView(Context context) {
        super(context);
        final int W = 200;
        final int H = 200;
        mBitmap = Bitmap.createBitmap(W, H, Bitmap.Config.RGB_565);
        mStartTime = System.currentTimeMillis();
    }
    @Override protected void onDraw(Canvas canvas) {
        renderPlasma(mBitmap, System.currentTimeMillis() - mStartTime);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        invalidate();
    }
}
