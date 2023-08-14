public final class TextInputMethodQualifier extends ResourceQualifier {
    public static final String NAME = "Text Input Method";
    private TextInputMethod mValue;
    public static enum TextInputMethod {
        NOKEY("nokeys", "No Keys"), 
        QWERTY("qwerty", "Qwerty"), 
        TWELVEKEYS("12key", "12 Key"); 
        private String mValue;
        private String mDisplayValue;
        private TextInputMethod(String value, String displayValue) {
            mValue = value;
            mDisplayValue = displayValue;
        }
        public static TextInputMethod getEnum(String value) {
            for (TextInputMethod orient : values()) {
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
        public static int getIndex(TextInputMethod value) {
            int i = 0;
            for (TextInputMethod input : values()) {
                if (value == input) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static TextInputMethod getByIndex(int index) {
            int i = 0;
            for (TextInputMethod value : values()) {
                if (i == index) {
                    return value;
                }
                i++;
            }
            return null;
        }
    }
    public TextInputMethodQualifier() {
    }
    public TextInputMethodQualifier(TextInputMethod value) {
        mValue = value;
    }
    public TextInputMethod getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Text Input";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("text_input"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        TextInputMethod method = TextInputMethod.getEnum(value);
        if (method != null) {
            TextInputMethodQualifier qualifier = new TextInputMethodQualifier();
            qualifier.mValue = method;
            config.setTextInputMethodQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof TextInputMethodQualifier) {
            return mValue == ((TextInputMethodQualifier)qualifier).mValue;
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
