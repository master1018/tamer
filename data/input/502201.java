public class Galaxy extends Activity {
    private GalaxyView mView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new GalaxyView(this);
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
