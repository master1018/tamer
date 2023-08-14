public class FakePhoneActivity extends Activity {
    private static final String TAG = "FakePhoneActivity";
    private Button mPlaceCall;
    private EditText mPhoneNumber;
    SimulatedRadioControl mRadioControl;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.fake_phone_activity);
        mPlaceCall = (Button) findViewById(R.id.placeCall);
        mPlaceCall.setOnClickListener(new ButtonListener());
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        mPhoneNumber.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        mPlaceCall.requestFocus();
                    }
                });
        mRadioControl = PhoneApp.getInstance().phone.getSimulatedRadioControl();
        Log.i(TAG, "- PhoneApp.getInstance(): " + PhoneApp.getInstance());
        Log.i(TAG, "- PhoneApp.getInstance().phone: " + PhoneApp.getInstance().phone);
        Log.i(TAG, "- mRadioControl: " + mRadioControl);
    }
    private class ButtonListener implements OnClickListener {
        public void onClick(View v) {
            if (mRadioControl == null) {
                Log.e("Phone", "SimulatedRadioControl not available, abort!");
                NotificationManager nm =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Toast.makeText(FakePhoneActivity.this, "null mRadioControl!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mRadioControl.triggerRing(mPhoneNumber.getText().toString());
        }
    }
}
