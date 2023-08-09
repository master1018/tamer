public class IccMissingPanel extends IccPanel {
    public IccMissingPanel(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim_missing);
        ((Button) findViewById(R.id.continueView)).setOnClickListener(mButtonListener);
    }
    View.OnClickListener mButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismiss();
        }
    };
}
