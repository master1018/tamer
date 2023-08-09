public class AWTKeyStroke implements Serializable {
    private static final long serialVersionUID = -6430539691155161871L;
    private static final Map<AWTKeyStroke, AWTKeyStroke> cache = new HashMap<AWTKeyStroke, AWTKeyStroke>(); 
    private static final Map<Integer, String> keyEventTypesMap = new HashMap<Integer, String>(); 
    private static Constructor<?> subConstructor;
    static {
        keyEventTypesMap.put(new Integer(KeyEvent.KEY_PRESSED), "pressed"); 
        keyEventTypesMap.put(new Integer(KeyEvent.KEY_RELEASED), "released"); 
        keyEventTypesMap.put(new Integer(KeyEvent.KEY_TYPED), "typed"); 
    }
    private char keyChar;
    private int keyCode;
    private int modifiers;
    private boolean onKeyRelease;
    protected AWTKeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
        setAWTKeyStroke(keyChar, keyCode, modifiers, onKeyRelease);
    }
    private void setAWTKeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
        this.keyChar = keyChar;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.onKeyRelease = onKeyRelease;
    }
    protected AWTKeyStroke() {
        this(KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_UNDEFINED, 0, false);
    }
    @Override
    public int hashCode() {
        return modifiers + (keyCode != KeyEvent.VK_UNDEFINED ? keyCode : keyChar)
                + (onKeyRelease ? -1 : 0);
    }
    public final int getModifiers() {
        return modifiers;
    }
    @Override
    public final boolean equals(Object anObject) {
        if (anObject instanceof AWTKeyStroke) {
            AWTKeyStroke key = (AWTKeyStroke)anObject;
            return ((key.keyCode == keyCode) && (key.keyChar == keyChar)
                    && (key.modifiers == modifiers) && (key.onKeyRelease == onKeyRelease));
        }
        return false;
    }
    @Override
    public String toString() {
        int type = getKeyEventType();
        return InputEvent.getModifiersExText(getModifiers()) + " " + 
                keyEventTypesMap.get(new Integer(type)) + " " + 
                (type == KeyEvent.KEY_TYPED ? new String(new char[] {
                    keyChar
                }) : KeyEvent.getKeyText(keyCode));
    }
    public final int getKeyCode() {
        return keyCode;
    }
    public final char getKeyChar() {
        return keyChar;
    }
    private static AWTKeyStroke getAWTKeyStroke(char keyChar, int keyCode, int modifiers,
            boolean onKeyRelease) {
        AWTKeyStroke key = newInstance(keyChar, keyCode, modifiers, onKeyRelease);
        AWTKeyStroke value = cache.get(key);
        if (value == null) {
            value = key;
            cache.put(key, value);
        }
        return value;
    }
    private static AWTKeyStroke newInstance(char keyChar, int keyCode, int modifiers,
            boolean onKeyRelease) {
        AWTKeyStroke key;
        key = new AWTKeyStroke();
        int allModifiers = getAllModifiers(modifiers);
        key.setAWTKeyStroke(keyChar, keyCode, allModifiers, onKeyRelease);
        return key;
    }
    private static int addMask(int mod, int mask) {
        return ((mod & mask) != 0) ? (mod | mask) : mod;
    }
    static int getAllModifiers(int mod) {
        int allMod = mod;
        int shift = (InputEvent.SHIFT_MASK | InputEvent.SHIFT_DOWN_MASK);
        int ctrl = (InputEvent.CTRL_MASK | InputEvent.CTRL_DOWN_MASK);
        int meta = (InputEvent.META_MASK | InputEvent.META_DOWN_MASK);
        int alt = (InputEvent.ALT_MASK | InputEvent.ALT_DOWN_MASK);
        int altGr = (InputEvent.ALT_GRAPH_MASK | InputEvent.ALT_GRAPH_DOWN_MASK);
        allMod = addMask(allMod, shift);
        allMod = addMask(allMod, ctrl);
        allMod = addMask(allMod, meta);
        allMod = addMask(allMod, alt);
        allMod = addMask(allMod, altGr);
        return allMod;
    }
    public static AWTKeyStroke getAWTKeyStroke(String s) {
        if (s == null) {
            throw new IllegalArgumentException(Messages.getString("awt.65")); 
        }
        StringTokenizer tokenizer = new StringTokenizer(s);
        Boolean release = null;
        int modifiers = 0;
        int keyCode = KeyEvent.VK_UNDEFINED;
        char keyChar = KeyEvent.CHAR_UNDEFINED;
        boolean typed = false;
        long modifier = 0;
        String token = null;
        do {
            token = getNextToken(tokenizer);
            modifier = parseModifier(token);
            modifiers |= modifier;
        } while (modifier > 0);
        typed = parseTypedID(token);
        if (typed) {
            token = getNextToken(tokenizer);
            keyChar = parseTypedKey(token);
        }
        if (keyChar == KeyEvent.CHAR_UNDEFINED) {
            release = parsePressedReleasedID(token);
            if (release != null) {
                token = getNextToken(tokenizer);
            }
            keyCode = parseKey(token);
        }
        if (tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException(Messages.getString("awt.66")); 
        }
        return getAWTKeyStroke(keyChar, keyCode, modifiers, release == Boolean.TRUE);
    }
    private static String getNextToken(StringTokenizer tokenizer) {
        try {
            return tokenizer.nextToken();
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException(Messages.getString("awt.66")); 
        }
    }
    static int getKeyCode(String s) {
        try {
            Field vk = KeyEvent.class.getField("VK_" + s); 
            return vk.getInt(null);
        } catch (Exception e) {
            if (s.length() != 1) {
                throw new IllegalArgumentException(Messages.getString("awt.66")); 
            }
            return KeyEvent.VK_UNDEFINED;
        }
    }
    public static AWTKeyStroke getAWTKeyStroke(char keyChar) {
        return getAWTKeyStroke(keyChar, KeyEvent.VK_UNDEFINED, 0, false);
    }
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
        return getAWTKeyStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers, onKeyRelease);
    }
    public static AWTKeyStroke getAWTKeyStroke(Character keyChar, int modifiers) {
        if (keyChar == null) {
            throw new IllegalArgumentException(Messages.getString("awt.01", "keyChar")); 
        }
        return getAWTKeyStroke(keyChar.charValue(), KeyEvent.VK_UNDEFINED, modifiers, false);
    }
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers) {
        return getAWTKeyStroke(keyCode, modifiers, false);
    }
    public static AWTKeyStroke getAWTKeyStrokeForEvent(KeyEvent anEvent) {
        int id = anEvent.getID();
        char undef = KeyEvent.CHAR_UNDEFINED;
        char keyChar = (id == KeyEvent.KEY_TYPED ? anEvent.getKeyChar() : undef);
        int keyCode = (keyChar == undef ? anEvent.getKeyCode() : KeyEvent.VK_UNDEFINED);
        return getAWTKeyStroke(keyChar, keyCode, anEvent.getModifiersEx(),
                id == KeyEvent.KEY_RELEASED);
    }
    public final int getKeyEventType() {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return KeyEvent.KEY_TYPED;
        }
        return (onKeyRelease ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED);
    }
    public final boolean isOnKeyRelease() {
        return onKeyRelease;
    }
    protected Object readResolve() throws ObjectStreamException {
        return getAWTKeyStroke(this.keyChar, this.keyCode, this.modifiers, this.onKeyRelease);
    }
    protected static void registerSubclass(Class<?> subclass) {
    }
    private static long parseModifier(String strMod) {
        long modifiers = 0l;
        if (strMod.equals("shift")) { 
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        } else if (strMod.equals("control") || strMod.equals("ctrl")) { 
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        } else if (strMod.equals("meta")) { 
            modifiers |= InputEvent.META_DOWN_MASK;
        } else if (strMod.equals("alt")) { 
            modifiers |= InputEvent.ALT_DOWN_MASK;
        } else if (strMod.equals("altGraph")) { 
            modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        } else if (strMod.equals("button1")) { 
            modifiers |= InputEvent.BUTTON1_DOWN_MASK;
        } else if (strMod.equals("button2")) { 
            modifiers |= InputEvent.BUTTON2_DOWN_MASK;
        } else if (strMod.equals("button3")) { 
            modifiers |= InputEvent.BUTTON3_DOWN_MASK;
        }
        return modifiers;
    }
    private static boolean parseTypedID(String strTyped) {
        if (strTyped.equals("typed")) { 
            return true;
        }
        return false;
    }
    private static char parseTypedKey(String strChar) {
        char keyChar = KeyEvent.CHAR_UNDEFINED;
        if (strChar.length() != 1) {
            throw new IllegalArgumentException(Messages.getString("awt.66")); 
        }
        keyChar = strChar.charAt(0);
        return keyChar;
    }
    private static Boolean parsePressedReleasedID(String str) {
        if (str.equals("pressed")) { 
            return Boolean.FALSE;
        } else if (str.equals("released")) { 
            return Boolean.TRUE;
        }
        return null;
    }
    private static int parseKey(String strCode) {
        int keyCode = KeyEvent.VK_UNDEFINED;
        keyCode = getKeyCode(strCode);
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException(Messages.getString("awt.66")); 
        }
        return keyCode;
    }
}
