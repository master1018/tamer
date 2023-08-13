public class GL2JavaActivity extends Activity {
    GL2JavaView mView;
    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new GL2JavaView(getApplication());
	setContentView(mView);
    }
    @Override protected void onPause() {
        super.onPause();
        mView.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        mView.onResume();
    }
}
