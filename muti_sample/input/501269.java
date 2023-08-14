public class GLJNIActivity extends Activity {
    GLJNIView mView;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new GLJNIView(getApplication());
	    mView.setFocusableInTouchMode(true);
	    setContentView(mView);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }
}
