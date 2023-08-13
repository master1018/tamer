public class BluetoothOppBtErrorActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private String mErrorContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String mErrorTitle = intent.getStringExtra("title");
        mErrorContent = intent.getStringExtra("content");
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_alert;
        p.mTitle = mErrorTitle;
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bt_error_btn_ok);
        p.mPositiveButtonListener = this;
        setupAlert();
    }
    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        TextView contentView = (TextView)view.findViewById(R.id.content);
        contentView.setText(mErrorContent);
        return view;
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                break;
        }
    }
}
