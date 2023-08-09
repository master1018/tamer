public class DrawPoints extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Paint   mPaint = new Paint();
        private float[] mPts;
        private static final float SIZE = 300;
        private static final int SEGS = 32;
        private static final int X = 0;
        private static final int Y = 1;
        private void buildPoints() {
            final int ptCount = (SEGS + 1) * 2;
            mPts = new float[ptCount * 2];
            float value = 0;
            final float delta = SIZE / SEGS;
            for (int i = 0; i <= SEGS; i++) {
                mPts[i*4 + X] = SIZE - value;
                mPts[i*4 + Y] = 0;
                mPts[i*4 + X + 2] = 0;
                mPts[i*4 + Y + 2] = value;
                value += delta;
            }
        }
        public SampleView(Context context) {
            super(context);
            buildPoints();
        }
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            canvas.translate(10, 10);
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(0);
            canvas.drawLines(mPts, paint);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(3);
            canvas.drawPoints(mPts, paint);
        }
    }
}
