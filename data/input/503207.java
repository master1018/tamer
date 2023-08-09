public class AnimateDrawables extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private AnimateDrawable mDrawable;
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            Drawable dr = context.getResources().getDrawable(R.drawable.beach);
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            Animation an = new TranslateAnimation(0, 100, 0, 200);
            an.setDuration(2000);
            an.setRepeatCount(-1);
            an.initialize(10, 10, 10, 10);
            mDrawable = new AnimateDrawable(dr, an);
            an.startNow();
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            mDrawable.draw(canvas);
            invalidate();
        }
    }
}
