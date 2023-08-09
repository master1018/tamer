public class ChooseLockPatternTutorial extends Activity implements View.OnClickListener {
    private View mNextButton;
    private View mSkipButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
        if (savedInstanceState == null && lockPatternUtils.isPatternEverChosen()) {
            Intent intent = new Intent(this, ChooseLockPattern.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        } else {
            initViews();
        }
    }
    private void initViews() {
        setContentView(R.layout.choose_lock_pattern_tutorial);
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mSkipButton = findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        if (v == mSkipButton) {
            setResult(ChooseLockPattern.RESULT_FINISHED);
            finish();
        } else if (v == mNextButton) {
            Intent intent = new Intent(this, ChooseLockPatternExample.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }
    }
}
