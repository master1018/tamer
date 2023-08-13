public class Grass extends Activity {
    private GrassView mView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new GrassView(this);
        setContentView(mView);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
        Runtime.getRuntime().exit(0);
    }
}