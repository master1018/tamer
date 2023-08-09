public class AccountSyncSettingsInAddAccount extends AccountSyncSettings 
        implements OnClickListener {
    private View mFinishArea;
    private View mFinishButton;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mRemoveAccountArea.setVisibility(View.GONE);
        mFinishArea = (View) findViewById(R.id.finish_button_area);
        mFinishArea.setVisibility(View.VISIBLE);
        mFinishButton = (View) findViewById(R.id.finish_button);
        mFinishButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        finish();
    }
}
