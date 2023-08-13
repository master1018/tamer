public class ChronometerStubActivity extends Activity {
    private Chronometer chronometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronometer_stub_layout);
        chronometer = (Chronometer) findViewById(R.id.test_chronometer);
    }
    public Chronometer getChronometer() {
        return chronometer;
    }
    public ViewGroup getViewGroup() {
        return (ViewGroup) findViewById(R.id.chronometer_view_group);
    }
}
