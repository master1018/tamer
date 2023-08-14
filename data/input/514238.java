public class ProgressBar4 extends Activity {
    private boolean mToggleIndeterminate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.progressbar_4);
        setProgressBarIndeterminateVisibility(mToggleIndeterminate);
        Button button = (Button) findViewById(R.id.toggle);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mToggleIndeterminate = !mToggleIndeterminate;
                setProgressBarIndeterminateVisibility(mToggleIndeterminate);
            }
        });
    }
}
