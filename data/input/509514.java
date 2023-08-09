public final class KeyboardStateQualifier extends ResourceQualifier {
    public static final String NAME = "Keyboard State";
    private KeyboardState mValue = null;
    public static enum KeyboardState {
        EXPOSED("keysexposed", "Exposed"), 
        HIDDEN("keyshidden", "Hidden"),    
        SOFT("keyssoft", "Soft");          
        private String mValue;
        private String mDisplayValue;
        private KeyboardState(String value, String displayValue) {
            mValue = value;
            mDisplayValue = displayValue;
        }
        public static KeyboardState getEnum(String value) {
            for (KeyboardState orient : values()) {
                if (orient.mValue.equals(value)) {
                    return orient;
                }
            }
            return null;
        }
        public String getValue() {
            return mValue;
        }
        public String getDisplayValue() {
            return mDisplayValue;
        }
        public static int getIndex(KeyboardState value) {
            int i = 0;
            for (KeyboardState input : values()) {
                if (value == input) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static KeyboardState getByIndex(int index) {
            int i = 0;
            for (KeyboardState value : values()) {
                if (i == index) {
                    return value;
                }
                i++;
            }
            return null;
        }
    }
    public KeyboardStateQualifier() {
    }
    public KeyboardStateQualifier(KeyboardState value) {
        mValue = value;
    }
    public KeyboardState getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Keyboard";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("keyboard"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        KeyboardState orientation = KeyboardState.getEnum(value);
        if (orientation != null) {
            KeyboardStateQualifier qualifier = new KeyboardStateQualifier();
            qualifier.mValue = orientation;
            config.setKeyboardStateQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean isMatchFor(ResourceQualifier qualifier) {
        if (qualifier instanceof KeyboardStateQualifier) {
            KeyboardStateQualifier referenceQualifier = (KeyboardStateQualifier)qualifier;
            if (referenceQualifier.mValue == KeyboardState.SOFT &&
                    mValue == KeyboardState.EXPOSED) {
                return true;
            }
            return referenceQualifier.mValue == mValue;
        }
        return false;
    }
    @Override
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        if (compareTo == null) {
            return true;
        }
        KeyboardStateQualifier compareQualifier = (KeyboardStateQualifier)compareTo;
        KeyboardStateQualifier referenceQualifier = (KeyboardStateQualifier)reference;
        if (referenceQualifier.mValue == KeyboardState.SOFT) { 
            if (compareQualifier.mValue == KeyboardState.EXPOSED && mValue == KeyboardState.SOFT) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof KeyboardStateQualifier) {
            return mValue == ((KeyboardStateQualifier)qualifier).mValue;
        }
        return false;
    }
    @Override
    public int hashCode() {
        if (mValue != null) {
            return mValue.hashCode();
        }
        return 0;
    }
    @Override
    public String getFolderSegment(IAndroidTarget target) {
        if (mValue != null) {
            return mValue.getValue();
        }
        return ""; 
    }
    @Override
    public String getStringValue() {
        if (mValue != null) {
            return mValue.getDisplayValue();
        }
        return ""; 
    }
}
