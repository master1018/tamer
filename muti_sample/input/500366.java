public class ClipboardService extends IClipboard.Stub {
    private CharSequence mClipboard = "";
    public ClipboardService(Context context) { }
    public void setClipboardText(CharSequence text) {
        synchronized (this) {
            if (text == null) {
                text = "";
            }
            mClipboard = text;
        }
    }
    public CharSequence getClipboardText() {
        synchronized (this) {
            return mClipboard;
        }
    }
    public boolean hasClipboardText() {
        synchronized (this) {
            return mClipboard.length() > 0;
        }
    }
}
