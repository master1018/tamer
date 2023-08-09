public class RunQueue extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {
    public boolean runnableRan = false;
    public boolean runnableCancelled = true;
    public boolean globalLayout = false;
    public ViewTreeObserver viewTreeObserver;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        TextView textView = new TextView(this);
        textView.setText("RunQueue");
        textView.setId(R.id.simple_view);
        setContentView(textView);
        final View view = findViewById(R.id.simple_view);
        view.post(new Runnable() {
            public void run() {
                runnableRan = true;
            }
        });
        final Runnable runnable = new Runnable() {
            public void run() {
                runnableCancelled = false;
            }
        };
        view.post(runnable);
        view.post(runnable);
        view.post(runnable);
        view.post(runnable);
        view.removeCallbacks(runnable);
        viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
    }
    public void onGlobalLayout() {
        globalLayout = true;
    }
}
