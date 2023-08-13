public class ClipboardManager {
    private static IClipboard sService;
    private Context mContext;
    static private IClipboard getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("clipboard");
        sService = IClipboard.Stub.asInterface(b);
        return sService;
    }
    public ClipboardManager(Context context, Handler handler) {
        mContext = context;
    }
    public CharSequence getText() {
        try {
            return getService().getClipboardText();
        } catch (RemoteException e) {
            return null;
        }
    }
    public void setText(CharSequence text) {
        try {
            getService().setClipboardText(text);
        } catch (RemoteException e) {
        }
    }
    public boolean hasText() {
        try {
            return getService().hasClipboardText();
        } catch (RemoteException e) {
            return false;
        }
    }
}
