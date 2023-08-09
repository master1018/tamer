public class DialerKeyListener extends NumberKeyListener
{
    @Override
    protected char[] getAcceptedChars()
    {
        return CHARACTERS;
    }
    public static DialerKeyListener getInstance() {
        if (sInstance != null)
            return sInstance;
        sInstance = new DialerKeyListener();
        return sInstance;
    }
    public int getInputType() {
        return InputType.TYPE_CLASS_PHONE;
    }
    protected int lookup(KeyEvent event, Spannable content) {
        int meta = getMetaState(content);
        int number = event.getNumber();
        if ((meta & (KeyEvent.META_ALT_ON | KeyEvent.META_SHIFT_ON)) == 0) {
            if (number != 0) {
                return number;
            }
        }
        int match = super.lookup(event, content);
        if (match != 0) {
            return match;
        } else {
            if (meta != 0) {
                KeyData kd = new KeyData();
                char[] accepted = getAcceptedChars();
                if (event.getKeyData(kd)) {
                    for (int i = 1; i < kd.meta.length; i++) {
                        if (ok(accepted, kd.meta[i])) {
                            return kd.meta[i];
                        }
                    }
                }
            }
            return number;
        }
    }
    public static final char[] CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '*',
            '+', '-', '(', ')', ',', '/', 'N', '.', ' ', ';'
        };
    private static DialerKeyListener sInstance;
}
