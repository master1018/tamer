class SoftInputWindow extends Dialog {
    final KeyEvent.DispatcherState mDispatcherState;
    public void setToken(IBinder token) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.token = token;
        getWindow().setAttributes(lp);
    }
    public SoftInputWindow(Context context, int theme,
            KeyEvent.DispatcherState dispatcherState) {
        super(context, theme);
        mDispatcherState = dispatcherState;
        initDockWindow();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mDispatcherState.reset();
    }
    public int getSize() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM) {
            return lp.height;
        } else {
            return lp.width;
        }
    }
    public void setSize(int size) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM) {
            lp.width = -1;
            lp.height = size;
        } else {
            lp.width = size;
            lp.height = -1;
        }
        getWindow().setAttributes(lp);
    }
    public void setGravity(int gravity) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        boolean oldIsVertical = (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM);
        lp.gravity = gravity;
        boolean newIsVertical = (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM);
        if (oldIsVertical != newIsVertical) {
            int tmp = lp.width;
            lp.width = lp.height;
            lp.height = tmp;
            getWindow().setAttributes(lp);
        }
    }
    private void initDockWindow() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_INPUT_METHOD;
        lp.setTitle("InputMethod");
        lp.gravity = Gravity.BOTTOM;
        lp.width = -1;
        getWindow().setAttributes(lp);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
