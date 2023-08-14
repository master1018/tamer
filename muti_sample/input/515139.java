public class PolyToPoly extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Paint   mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Matrix  mMatrix = new Matrix();
        private Paint.FontMetrics mFontMetrics;
        private void doDraw(Canvas canvas, float src[], float dst[]) {
            canvas.save();
            mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
            canvas.concat(mMatrix);
            mPaint.setColor(Color.GRAY);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, 64, 64, mPaint);
            canvas.drawLine(0, 0, 64, 64, mPaint);
            canvas.drawLine(0, 64, 64, 0, mPaint);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL);
            float x = 64/2;
            float y = 64/2 - (mFontMetrics.ascent + mFontMetrics.descent)/2;
            canvas.drawText(src.length/2 + "", x, y, mPaint);
            canvas.restore();
        }
        public SampleView(Context context) {
            super(context);
            mPaint.setStrokeWidth(4);
            mPaint.setTextSize(40);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mFontMetrics = mPaint.getFontMetrics();
        }
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            canvas.drawColor(Color.WHITE);
            canvas.save();
            canvas.translate(10, 10);
            doDraw(canvas, new float[] { 0, 0 }, new float[] { 5, 5 });
            canvas.restore();
            canvas.save();
            canvas.translate(160, 10);
            doDraw(canvas, new float[] { 32, 32, 64, 32 },
                           new float[] { 32, 32, 64, 48 });
            canvas.restore();
            canvas.save();
            canvas.translate(10, 110);
            doDraw(canvas, new float[] { 0, 0, 64, 0, 0, 64 },
                           new float[] { 0, 0, 96, 0, 24, 64 });
            canvas.restore();
            canvas.save();
            canvas.translate(160, 110);
            doDraw(canvas, new float[] { 0, 0, 64, 0, 64, 64, 0, 64 },
                           new float[] { 0, 0, 96, 0, 64, 96, 0, 64 });
            canvas.restore();
        }
    }
}
