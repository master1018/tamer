public class KeyCharacterMap
{
    public static final int BUILT_IN_KEYBOARD = 0;
    public static final int NUMERIC = 1;
    public static final int PREDICTIVE = 2;
    public static final int ALPHA = 3;
    public static final char HEX_INPUT = '\uEF00';
    public static final char PICKER_DIALOG_INPUT = '\uEF01';
    private static Object sLock = new Object();
    private static SparseArray<WeakReference<KeyCharacterMap>> sInstances 
        = new SparseArray<WeakReference<KeyCharacterMap>>();
    public static KeyCharacterMap load(int keyboard)
    {
        synchronized (sLock) {
            KeyCharacterMap result;
            WeakReference<KeyCharacterMap> ref = sInstances.get(keyboard);
            if (ref != null) {
                result = ref.get();
                if (result != null) {
                    return result;
                }
            }
            result = new KeyCharacterMap(keyboard);
            sInstances.put(keyboard, new WeakReference<KeyCharacterMap>(result));
            return result;
        }
    }
    private KeyCharacterMap(int keyboardDevice)
    {
        mKeyboardDevice = keyboardDevice;
        mPointer = ctor_native(keyboardDevice);
    }
    public int get(int keyCode, int meta)
    {
        if ((meta & MetaKeyKeyListener.META_CAP_LOCKED) != 0) {
            meta |= KeyEvent.META_SHIFT_ON;
        }
        if ((meta & MetaKeyKeyListener.META_ALT_LOCKED) != 0) {
            meta |= KeyEvent.META_ALT_ON;
        }
        if ((meta & MetaKeyKeyListener.META_CAP_LOCKED) != 0) {
            if (get_native(mPointer, keyCode, KeyEvent.META_SHIFT_ON) ==
                get_native(mPointer, keyCode, KeyEvent.META_ALT_ON)) {
                meta &= ~KeyEvent.META_SHIFT_ON;
            }
        }
        int ret = get_native(mPointer, keyCode, meta);
        int map = COMBINING.get(ret);
        if (map != 0) {
            return map;
        } else {
            return ret;
        }
    }
    public char getNumber(int keyCode)
    {
        return getNumber_native(mPointer, keyCode);
    }
    public char getMatch(int keyCode, char[] chars)
    {
        return getMatch(keyCode, chars, 0);
    }
    public char getMatch(int keyCode, char[] chars, int modifiers)
    {
        if (chars == null) {
            throw new NullPointerException();
        }
        return getMatch_native(mPointer, keyCode, chars, modifiers);
    }
    public char getDisplayLabel(int keyCode)
    {
        return getDisplayLabel_native(mPointer, keyCode);
    }
    public static int getDeadChar(int accent, int c)
    {
        return DEAD.get((accent << 16) | c);
    }
    public static class KeyData {
        public static final int META_LENGTH = 4;
        public char displayLabel;
        public char number;
        public char[] meta = new char[META_LENGTH];
    }
    public boolean getKeyData(int keyCode, KeyData results)
    {
        if (results.meta.length >= KeyData.META_LENGTH) {
            return getKeyData_native(mPointer, keyCode, results);
        } else {
            throw new IndexOutOfBoundsException("results.meta.length must be >= " +
                                                KeyData.META_LENGTH);
        }
    }
    public KeyEvent[] getEvents(char[] chars)
    {
        if (chars == null) {
            throw new NullPointerException();
        }
        long[] keys = getEvents_native(mPointer, chars);
        if (keys == null) {
            return null;
        }
        int len = keys.length*2;
        int N = keys.length;
        for (int i=0; i<N; i++) {
            int mods = (int)(keys[i] >> 32);
            if ((mods & KeyEvent.META_ALT_ON) != 0) {
                len += 2;
            }
            if ((mods & KeyEvent.META_SHIFT_ON) != 0) {
                len += 2;
            }
            if ((mods & KeyEvent.META_SYM_ON) != 0) {
                len += 2;
            }
        }
        KeyEvent[] rv = new KeyEvent[len];
        int index = 0;
        long now = SystemClock.uptimeMillis();
        int device = mKeyboardDevice;
        for (int i=0; i<N; i++) {
            int mods = (int)(keys[i] >> 32);
            int meta = 0;
            if ((mods & KeyEvent.META_ALT_ON) != 0) {
                meta |= KeyEvent.META_ALT_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ALT_LEFT, 0, meta, device, 0);
                index++;
            }
            if ((mods & KeyEvent.META_SHIFT_ON) != 0) {
                meta |= KeyEvent.META_SHIFT_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_SHIFT_LEFT, 0, meta, device, 0);
                index++;
            }
            if ((mods & KeyEvent.META_SYM_ON) != 0) {
                meta |= KeyEvent.META_SYM_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_SYM, 0, meta, device, 0);
                index++;
            }
            int key = (int)(keys[i]);
            rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                    key, 0, meta, device, 0);
            index++;
            rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_UP,
                    key, 0, meta, device, 0);
            index++;
            if ((mods & KeyEvent.META_ALT_ON) != 0) {
                meta &= ~KeyEvent.META_ALT_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_ALT_LEFT, 0, meta, device, 0);
                index++;
            }
            if ((mods & KeyEvent.META_SHIFT_ON) != 0) {
                meta &= ~KeyEvent.META_SHIFT_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_SHIFT_LEFT, 0, meta, device, 0);
                index++;
            }
            if ((mods & KeyEvent.META_SYM_ON) != 0) {
                meta &= ~KeyEvent.META_SYM_ON;
                rv[index] = new KeyEvent(now, now, KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_SYM, 0, meta, device, 0);
                index++;
            }
        }
        return rv;
    }
    public boolean isPrintingKey(int keyCode)
    {
        int type = Character.getType(get(keyCode, 0));
        switch (type)
        {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.CONTROL:
            case Character.FORMAT:
                return false;
            default:
                return true;
        }
    }
    protected void finalize() throws Throwable
    {
        dtor_native(mPointer);
    }
    public int getKeyboardType()
    {
        return getKeyboardType_native(mPointer);
    }
    public static boolean deviceHasKey(int keyCode) {
        int[] codeArray = new int[1];
        codeArray[0] = keyCode;
        boolean[] ret = deviceHasKeys(codeArray);
        return ret[0];
    }
    public static boolean[] deviceHasKeys(int[] keyCodes) {
        boolean[] ret = new boolean[keyCodes.length];
        IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        try {
            wm.hasKeys(keyCodes, ret);
        } catch (RemoteException e) {
        }
        return ret;
    }
    private int mPointer;
    private int mKeyboardDevice;
    private static native int ctor_native(int id);
    private static native void dtor_native(int ptr);
    private static native char get_native(int ptr, int keycode,
                                    int meta);
    private static native char getNumber_native(int ptr, int keycode);
    private static native char getMatch_native(int ptr, int keycode,
                                    char[] chars, int modifiers);
    private static native char getDisplayLabel_native(int ptr, int keycode);
    private static native boolean getKeyData_native(int ptr, int keycode,
                                    KeyData results);
    private static native int getKeyboardType_native(int ptr);
    private static native long[] getEvents_native(int ptr, char[] str);
    private static SparseIntArray COMBINING = new SparseIntArray();
    private static SparseIntArray DEAD = new SparseIntArray();
    private static final int ACUTE = '\u00B4' << 16;
    private static final int GRAVE = '`' << 16;
    private static final int CIRCUMFLEX = '^' << 16;
    private static final int TILDE = '~' << 16;
    private static final int UMLAUT = '\u00A8' << 16;
    public static final int COMBINING_ACCENT = 0x80000000;
    public static final int COMBINING_ACCENT_MASK = 0x7FFFFFFF;
    static {
        COMBINING.put('\u0300', (GRAVE >> 16) | COMBINING_ACCENT);
        COMBINING.put('\u0301', (ACUTE >> 16) | COMBINING_ACCENT);
        COMBINING.put('\u0302', (CIRCUMFLEX >> 16) | COMBINING_ACCENT);
        COMBINING.put('\u0303', (TILDE >> 16) | COMBINING_ACCENT);
        COMBINING.put('\u0308', (UMLAUT >> 16) | COMBINING_ACCENT);
        DEAD.put(ACUTE | 'A', '\u00C1');
        DEAD.put(ACUTE | 'C', '\u0106');
        DEAD.put(ACUTE | 'E', '\u00C9');
        DEAD.put(ACUTE | 'G', '\u01F4');
        DEAD.put(ACUTE | 'I', '\u00CD');
        DEAD.put(ACUTE | 'K', '\u1E30');
        DEAD.put(ACUTE | 'L', '\u0139');
        DEAD.put(ACUTE | 'M', '\u1E3E');
        DEAD.put(ACUTE | 'N', '\u0143');
        DEAD.put(ACUTE | 'O', '\u00D3');
        DEAD.put(ACUTE | 'P', '\u1E54');
        DEAD.put(ACUTE | 'R', '\u0154');
        DEAD.put(ACUTE | 'S', '\u015A');
        DEAD.put(ACUTE | 'U', '\u00DA');
        DEAD.put(ACUTE | 'W', '\u1E82');
        DEAD.put(ACUTE | 'Y', '\u00DD');
        DEAD.put(ACUTE | 'Z', '\u0179');
        DEAD.put(ACUTE | 'a', '\u00E1');
        DEAD.put(ACUTE | 'c', '\u0107');
        DEAD.put(ACUTE | 'e', '\u00E9');
        DEAD.put(ACUTE | 'g', '\u01F5');
        DEAD.put(ACUTE | 'i', '\u00ED');
        DEAD.put(ACUTE | 'k', '\u1E31');
        DEAD.put(ACUTE | 'l', '\u013A');
        DEAD.put(ACUTE | 'm', '\u1E3F');
        DEAD.put(ACUTE | 'n', '\u0144');
        DEAD.put(ACUTE | 'o', '\u00F3');
        DEAD.put(ACUTE | 'p', '\u1E55');
        DEAD.put(ACUTE | 'r', '\u0155');
        DEAD.put(ACUTE | 's', '\u015B');
        DEAD.put(ACUTE | 'u', '\u00FA');
        DEAD.put(ACUTE | 'w', '\u1E83');
        DEAD.put(ACUTE | 'y', '\u00FD');
        DEAD.put(ACUTE | 'z', '\u017A');
        DEAD.put(CIRCUMFLEX | 'A', '\u00C2');
        DEAD.put(CIRCUMFLEX | 'C', '\u0108');
        DEAD.put(CIRCUMFLEX | 'E', '\u00CA');
        DEAD.put(CIRCUMFLEX | 'G', '\u011C');
        DEAD.put(CIRCUMFLEX | 'H', '\u0124');
        DEAD.put(CIRCUMFLEX | 'I', '\u00CE');
        DEAD.put(CIRCUMFLEX | 'J', '\u0134');
        DEAD.put(CIRCUMFLEX | 'O', '\u00D4');
        DEAD.put(CIRCUMFLEX | 'S', '\u015C');
        DEAD.put(CIRCUMFLEX | 'U', '\u00DB');
        DEAD.put(CIRCUMFLEX | 'W', '\u0174');
        DEAD.put(CIRCUMFLEX | 'Y', '\u0176');
        DEAD.put(CIRCUMFLEX | 'Z', '\u1E90');
        DEAD.put(CIRCUMFLEX | 'a', '\u00E2');
        DEAD.put(CIRCUMFLEX | 'c', '\u0109');
        DEAD.put(CIRCUMFLEX | 'e', '\u00EA');
        DEAD.put(CIRCUMFLEX | 'g', '\u011D');
        DEAD.put(CIRCUMFLEX | 'h', '\u0125');
        DEAD.put(CIRCUMFLEX | 'i', '\u00EE');
        DEAD.put(CIRCUMFLEX | 'j', '\u0135');
        DEAD.put(CIRCUMFLEX | 'o', '\u00F4');
        DEAD.put(CIRCUMFLEX | 's', '\u015D');
        DEAD.put(CIRCUMFLEX | 'u', '\u00FB');
        DEAD.put(CIRCUMFLEX | 'w', '\u0175');
        DEAD.put(CIRCUMFLEX | 'y', '\u0177');
        DEAD.put(CIRCUMFLEX | 'z', '\u1E91');
        DEAD.put(GRAVE | 'A', '\u00C0');
        DEAD.put(GRAVE | 'E', '\u00C8');
        DEAD.put(GRAVE | 'I', '\u00CC');
        DEAD.put(GRAVE | 'N', '\u01F8');
        DEAD.put(GRAVE | 'O', '\u00D2');
        DEAD.put(GRAVE | 'U', '\u00D9');
        DEAD.put(GRAVE | 'W', '\u1E80');
        DEAD.put(GRAVE | 'Y', '\u1EF2');
        DEAD.put(GRAVE | 'a', '\u00E0');
        DEAD.put(GRAVE | 'e', '\u00E8');
        DEAD.put(GRAVE | 'i', '\u00EC');
        DEAD.put(GRAVE | 'n', '\u01F9');
        DEAD.put(GRAVE | 'o', '\u00F2');
        DEAD.put(GRAVE | 'u', '\u00F9');
        DEAD.put(GRAVE | 'w', '\u1E81');
        DEAD.put(GRAVE | 'y', '\u1EF3');
        DEAD.put(TILDE | 'A', '\u00C3');
        DEAD.put(TILDE | 'E', '\u1EBC');
        DEAD.put(TILDE | 'I', '\u0128');
        DEAD.put(TILDE | 'N', '\u00D1');
        DEAD.put(TILDE | 'O', '\u00D5');
        DEAD.put(TILDE | 'U', '\u0168');
        DEAD.put(TILDE | 'V', '\u1E7C');
        DEAD.put(TILDE | 'Y', '\u1EF8');
        DEAD.put(TILDE | 'a', '\u00E3');
        DEAD.put(TILDE | 'e', '\u1EBD');
        DEAD.put(TILDE | 'i', '\u0129');
        DEAD.put(TILDE | 'n', '\u00F1');
        DEAD.put(TILDE | 'o', '\u00F5');
        DEAD.put(TILDE | 'u', '\u0169');
        DEAD.put(TILDE | 'v', '\u1E7D');
        DEAD.put(TILDE | 'y', '\u1EF9');
        DEAD.put(UMLAUT | 'A', '\u00C4');
        DEAD.put(UMLAUT | 'E', '\u00CB');
        DEAD.put(UMLAUT | 'H', '\u1E26');
        DEAD.put(UMLAUT | 'I', '\u00CF');
        DEAD.put(UMLAUT | 'O', '\u00D6');
        DEAD.put(UMLAUT | 'U', '\u00DC');
        DEAD.put(UMLAUT | 'W', '\u1E84');
        DEAD.put(UMLAUT | 'X', '\u1E8C');
        DEAD.put(UMLAUT | 'Y', '\u0178');
        DEAD.put(UMLAUT | 'a', '\u00E4');
        DEAD.put(UMLAUT | 'e', '\u00EB');
        DEAD.put(UMLAUT | 'h', '\u1E27');
        DEAD.put(UMLAUT | 'i', '\u00EF');
        DEAD.put(UMLAUT | 'o', '\u00F6');
        DEAD.put(UMLAUT | 't', '\u1E97');
        DEAD.put(UMLAUT | 'u', '\u00FC');
        DEAD.put(UMLAUT | 'w', '\u1E85');
        DEAD.put(UMLAUT | 'x', '\u1E8D');
        DEAD.put(UMLAUT | 'y', '\u00FF');
    }
}
