public class ListUtil {
    private final ListView mListView;
    private final Instrumentation mInstrumentation;
    public ListUtil(ListView listView, Instrumentation instrumentation) {
        mListView = listView;
        mInstrumentation = instrumentation;
    }
    public final void setSelectedPosition(final int pos) {
        mListView.post(new Runnable() {
            public void run() {
                mListView.setSelection(pos);
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    public final int getListTop() {
        return mListView.getListPaddingTop();
    }
    public final int getListBottom() {
        return mListView.getHeight() - mListView.getListPaddingBottom();
    }
    public final void arrowScrollToSelectedPosition(int desiredPos) {
        if (desiredPos > mListView.getSelectedItemPosition()) {
            arrowDownToSelectedPosition(desiredPos);
        } else {
            arrowUpToSelectedPosition(desiredPos);
        }
    }
    private void arrowDownToSelectedPosition(int position) {
        int maxDowns = 20;
        while(mListView.getSelectedItemPosition() < position && --maxDowns > 0) {
            mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        if (position != mListView.getSelectedItemPosition()) {
            throw new IllegalStateException("couldn't get to item after 20 downs");
        }
    }
    private void arrowUpToSelectedPosition(int position) {
        int maxUps = 20;
        while(mListView.getSelectedItemPosition() > position && --maxUps > 0) {
            mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_DPAD_UP);
        }
        if (position != mListView.getSelectedItemPosition()) {
            throw new IllegalStateException("couldn't get to item after 20 ups");
        }
    }
}
