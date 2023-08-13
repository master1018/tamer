public class BrightnessLimit extends Activity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_limit);
        Button b = (Button) findViewById(R.id.go);
        b.setOnClickListener(this);
    }
    public void onClick(View v) {
        IPowerManager power = IPowerManager.Stub.asInterface(
                ServiceManager.getService("power"));
        if (power != null) {
            try {
                power.setBacklightBrightness(0);
            } catch (RemoteException darn) {
            }
        }
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
    }
}
