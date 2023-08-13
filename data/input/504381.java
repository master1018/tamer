public class ProgressBar1 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.progressbar_1);
        setProgressBarVisibility(true);
        final ProgressBar progressHorizontal = (ProgressBar) findViewById(R.id.progress_horizontal);
        setProgress(progressHorizontal.getProgress() * 100);
        setSecondaryProgress(progressHorizontal.getSecondaryProgress() * 100);
        Button button = (Button) findViewById(R.id.increase);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                progressHorizontal.incrementProgressBy(1);
                setProgress(100 * progressHorizontal.getProgress());
            }
        });
        button = (Button) findViewById(R.id.decrease);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                progressHorizontal.incrementProgressBy(-1);
                setProgress(100 * progressHorizontal.getProgress());
            }
        });
        button = (Button) findViewById(R.id.increase_secondary);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                progressHorizontal.incrementSecondaryProgressBy(1);
                setSecondaryProgress(100 * progressHorizontal.getSecondaryProgress());
            }
        });
        button = (Button) findViewById(R.id.decrease_secondary);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                progressHorizontal.incrementSecondaryProgressBy(-1);
                setSecondaryProgress(100 * progressHorizontal.getSecondaryProgress());
            }
        });
    }
}
