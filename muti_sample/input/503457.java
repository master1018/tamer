public class ScreenSizeQualifier extends ResourceQualifier {
    public static final String NAME = "Screen Size";
    private ScreenSize mValue = null;
    public static enum ScreenSize {
        SMALL("small", "Small"), 
        NORMAL("normal", "Normal"), 
        LARGE("large", "Large"); 
        private String mValue;
        private String mDisplayValue;
        private ScreenSize(String value, String displayValue) {
            mValue = value;
            mDisplayValue = displayValue;
        }
        public static ScreenSize getEnum(String value) {
            for (ScreenSize orient : values()) {
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
        public static int getIndex(ScreenSize orientation) {
            int i = 0;
            for (ScreenSize orient : values()) {
                if (orient == orientation) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static ScreenSize getByIndex(int index) {
            int i = 0;
            for (ScreenSize orient : values()) {
                if (i == index) {
                    return orient;
                }
                i++;
            }
            return null;
        }
    }
    public ScreenSizeQualifier() {
    }
    public ScreenSizeQualifier(ScreenSize value) {
        mValue = value;
    }
    public ScreenSize getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Size";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("size"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        ScreenSize size = ScreenSize.getEnum(value);
        if (size != null) {
            ScreenSizeQualifier qualifier = new ScreenSizeQualifier(size);
            config.setScreenSizeQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof ScreenSizeQualifier) {
            return mValue == ((ScreenSizeQualifier)qualifier).mValue;
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
            if (target == null) {
                return mValue.getValue();
            }
            AndroidVersion version = target.getVersion();
            if (version.getApiLevel() >= 4 ||
                    (version.getApiLevel() == 3 && "Donut".equals(version.getCodename()))) {
                return mValue.getValue();
            }
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
