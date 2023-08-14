public class RequestFocus extends Activity {
    protected final Handler mHandler = new Handler();
    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.focus_after_removal);
        final Button bottomRightButton = (Button) findViewById(R.id.bottomRightButton);
        bottomRightButton.requestFocus();
        bottomRightButton.setText("I should have focus");
    }
    public Handler getHandler() {
        return mHandler;
    }
}
