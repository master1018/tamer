public class PathFillTypes extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Path mPath;
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPath = new Path();
            mPath.addCircle(40, 40, 45, Path.Direction.CCW);
            mPath.addCircle(80, 80, 45, Path.Direction.CCW);
        }
        private void showPath(Canvas canvas, int x, int y, Path.FillType ft,
                              Paint paint) {
            canvas.save();
            canvas.translate(x, y);
            canvas.clipRect(0, 0, 120, 120);
            canvas.drawColor(Color.WHITE);
            mPath.setFillType(ft);
            canvas.drawPath(mPath, paint);
            canvas.restore();
        }
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            canvas.drawColor(0xFFCCCCCC);
            canvas.translate(20, 20);
            paint.setAntiAlias(true);
            showPath(canvas, 0, 0, Path.FillType.WINDING, paint);
            showPath(canvas, 160, 0, Path.FillType.EVEN_ODD, paint);
            showPath(canvas, 0, 160, Path.FillType.INVERSE_WINDING, paint);
            showPath(canvas, 160, 160, Path.FillType.INVERSE_EVEN_ODD, paint);
        }
    }
}
