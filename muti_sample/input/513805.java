public class AlphaBitmap extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Bitmap mBitmap;
        private Bitmap mBitmap2;
        private Bitmap mBitmap3;
        private Shader mShader;
        private static void drawIntoBitmap(Bitmap bm) {
            float x = bm.getWidth();
            float y = bm.getHeight();
            Canvas c = new Canvas(bm);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setAlpha(0x80);
            c.drawCircle(x/2, y/2, x/2, p);
            p.setAlpha(0x30);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            p.setTextSize(60);
            p.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = p.getFontMetrics();
            c.drawText("Alpha", x/2, (y-fm.ascent)/2, p);
        }
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            InputStream is = context.getResources().openRawResource(R.drawable.app_sample_code);
            mBitmap = BitmapFactory.decodeStream(is);
            mBitmap2 = mBitmap.extractAlpha();
            mBitmap3 = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8);
            drawIntoBitmap(mBitmap3);
            mShader = new LinearGradient(0, 0, 100, 70, new int[] {
                                         Color.RED, Color.GREEN, Color.BLUE },
                                         null, Shader.TileMode.MIRROR);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            float y = 10;
            p.setColor(Color.RED);
            canvas.drawBitmap(mBitmap, 10, y, p);
            y += mBitmap.getHeight() + 10;
            canvas.drawBitmap(mBitmap2, 10, y, p);
            y += mBitmap2.getHeight() + 10;
            p.setShader(mShader);
            canvas.drawBitmap(mBitmap3, 10, y, p);
        }
    }
}
