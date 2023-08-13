public final class ScreenOrientationQualifier extends ResourceQualifier {
    public static final String NAME = "Screen Orientation";
    private ScreenOrientation mValue = null;
    public static enum ScreenOrientation {
        PORTRAIT("port", "Portrait"), 
        LANDSCAPE("land", "Landscape"), 
        SQUARE("square", "Square"); 
        private String mValue;
        private String mDisplayValue;
        private ScreenOrientation(String value, String displayValue) {
            mValue = value;
            mDisplayValue = displayValue;
        }
        public static ScreenOrientation getEnum(String value) {
            for (ScreenOrientation orient : values()) {
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
        public static int getIndex(ScreenOrientation orientation) {
            int i = 0;
            for (ScreenOrientation orient : values()) {
                if (orient == orientation) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static ScreenOrientation getByIndex(int index) {
            int i = 0;
            for (ScreenOrientation orient : values()) {
                if (i == index) {
                    return orient;
                }
                i++;
            }
            return null;
        }
    }
    public ScreenOrientationQualifier() {
    }
    public ScreenOrientationQualifier(ScreenOrientation value) {
        mValue = value;
    }
    public ScreenOrientation getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Orientation";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("orientation"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        ScreenOrientation orientation = ScreenOrientation.getEnum(value);
        if (orientation != null) {
            ScreenOrientationQualifier qualifier = new ScreenOrientationQualifier(orientation);
            config.setScreenOrientationQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof ScreenOrientationQualifier) {
            return mValue == ((ScreenOrientationQualifier)qualifier).mValue;
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
