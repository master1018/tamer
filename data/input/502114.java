public class LLOfButtons1 extends Activity {
    private boolean mButtonPressed = false;
    private Button mFirstButton;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.linear_layout_buttons);
        mFirstButton = (Button) findViewById(R.id.button1);
        mFirstButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonPressed = true;
            }
        });
    }
    public LinearLayout getLayout() {
        return (LinearLayout) findViewById(R.id.layout);
    }
    public Button getFirstButton() {
        return mFirstButton;
    }
    public boolean buttonClickListenerFired() {
        return mButtonPressed;
    }
    public boolean isInTouchMode() {
        return mFirstButton.isInTouchMode();
    }
}
