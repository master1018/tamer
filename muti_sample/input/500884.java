public class Focus3 extends Activity {
    private Button mTopButton;
    private Button mBottomButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_3);
        mTopButton = (Button) findViewById(R.id.top);
        mBottomButton = (Button) findViewById(R.id.bottom);
    }
    public Button getTopButton() {
        return mTopButton;
    }
    public Button getBottomButton() {
        return mBottomButton;
    }
}
