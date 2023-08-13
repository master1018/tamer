public class DateTimeSettingsSetupWizard extends DateTimeSettings implements OnClickListener {
    private View mNextButton;
    @Override
    protected void onCreate(Bundle icicle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        super.onCreate(icicle);
        setContentView(R.layout.date_time_settings_setupwizard);
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        setResult(RESULT_OK);
        finish();
    }
}
