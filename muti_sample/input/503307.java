public class BitmapDecode extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Bitmap mBitmap;
        private Bitmap mBitmap2;
        private Bitmap mBitmap3;
        private Bitmap mBitmap4;
        private Drawable mDrawable;
        private Movie mMovie;
        private long mMovieStart;
        private static byte[] streamToBytes(InputStream is) {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
            } catch (java.io.IOException e) {
            }
            return os.toByteArray();
        }
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            java.io.InputStream is;
            is = context.getResources().openRawResource(R.drawable.beach);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm;
            opts.inJustDecodeBounds = true;
            bm = BitmapFactory.decodeStream(is, null, opts);
            opts.inJustDecodeBounds = false;    
            opts.inSampleSize = 4;             
            bm = BitmapFactory.decodeStream(is, null, opts);
            mBitmap = bm;
            is = context.getResources().openRawResource(R.drawable.frog);
            mBitmap2 = BitmapFactory.decodeStream(is);
            int w = mBitmap2.getWidth();
            int h = mBitmap2.getHeight();
            int[] pixels = new int[w*h];
            mBitmap2.getPixels(pixels, 0, w, 0, 0, w, h);
            mBitmap3 = Bitmap.createBitmap(pixels, 0, w, w, h,
                                           Bitmap.Config.ARGB_8888);
            mBitmap4 = Bitmap.createBitmap(pixels, 0, w, w, h,
                                           Bitmap.Config.ARGB_4444);
            mDrawable = context.getResources().getDrawable(R.drawable.button);
            mDrawable.setBounds(150, 20, 300, 100);
            is = context.getResources().openRawResource(R.drawable.animated_gif);
            if (true) {
                mMovie = Movie.decodeStream(is);
            } else {
                byte[] array = streamToBytes(is);
                mMovie = Movie.decodeByteArray(array, 0, array.length);
            }
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);            
            Paint p = new Paint();
            p.setAntiAlias(true);
            canvas.drawBitmap(mBitmap, 10, 10, null);
            canvas.drawBitmap(mBitmap2, 10, 170, null);
            canvas.drawBitmap(mBitmap3, 110, 170, null);
            canvas.drawBitmap(mBitmap4, 210, 170, null);
            mDrawable.draw(canvas);
            long now = android.os.SystemClock.uptimeMillis();
            if (mMovieStart == 0) {   
                mMovieStart = now;
            }
            if (mMovie != null) {
                int dur = mMovie.duration();
                if (dur == 0) {
                    dur = 1000;
                }
                int relTime = (int)((now - mMovieStart) % dur);
                mMovie.setTime(relTime);
                mMovie.draw(canvas, getWidth() - mMovie.width(),
                            getHeight() - mMovie.height());
                invalidate();
            }
        }
    }
}
