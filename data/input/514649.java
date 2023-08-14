public class Layers extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG |
                                            Canvas.CLIP_SAVE_FLAG |
                                            Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                                            Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                                            Canvas.CLIP_TO_LAYER_SAVE_FLAG;
        private Paint mPaint;
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);            
            canvas.translate(10, 10);
            canvas.saveLayerAlpha(0, 0, 200, 200, 0x88, LAYER_FLAGS);
            mPaint.setColor(Color.RED);
            canvas.drawCircle(75, 75, 75, mPaint);
            mPaint.setColor(Color.BLUE);
            canvas.drawCircle(125, 125, 75, mPaint);
            canvas.restore();
        }
    }
}
