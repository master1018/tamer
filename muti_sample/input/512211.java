public class BluetoothOppBtEnableActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_alert;
        p.mTitle = getString(R.string.bt_enable_title);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bt_enable_ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bt_enable_cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
    }
    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        TextView contentView = (TextView)view.findViewById(R.id.content);
        contentView.setText(getString(R.string.bt_enable_line1) + "\n\n"
                + getString(R.string.bt_enable_line2) + "\n");
        return view;
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                BluetoothOppManager mOppManager = BluetoothOppManager.getInstance(this);
                mOppManager.enableBluetooth(); 
                mOppManager.mSendingFlag = true;
                Toast.makeText(this, getString(R.string.enabling_progress_content),
                        Toast.LENGTH_SHORT).show();
                Intent in = new Intent(this, BluetoothOppBtEnablingActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(in);
                finish();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                finish();
                break;
        }
    }
}
