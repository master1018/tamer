class DTMFTwelveKeyDialerView extends LinearLayout {
    private static final String LOG_TAG = "PHONE/DTMFTwelveKeyDialerView";
    private static final boolean DBG = false;
    private DTMFTwelveKeyDialer mDialer;
    private ButtonGridLayout mButtonGrid;
    public DTMFTwelveKeyDialerView (Context context) {
        super(context);
    }
    public DTMFTwelveKeyDialerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    void setDialer (DTMFTwelveKeyDialer dialer) {
        mDialer = dialer;
        mButtonGrid = (ButtonGridLayout)findViewById(R.id.dialpad);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (DBG) log("dispatchKeyEvent(" + event + ")...");
        int keyCode = event.getKeyCode();
        if (mDialer != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_CALL:
                    return event.isDown() ? mDialer.onKeyDown(keyCode, event) :
                        mDialer.onKeyUp(keyCode, event);
            }
        }
        if (DBG) log("==> dispatchKeyEvent: forwarding event to the DTMFDialer");
        return super.dispatchKeyEvent(event);
    }
    public void setKeysBackgroundResource(int resid) {
        mButtonGrid.setChildrenBackgroundResource(resid);
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
