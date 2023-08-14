public class ScreenRatioQualifier extends ResourceQualifier {
    public static final String NAME = "Screen Ratio";
    private ScreenRatio mValue = null;
    public static enum ScreenRatio {
        NOTLONG("notlong", "Not Long"), 
        LONG("long", "Long"); 
        private String mValue;
        private String mDisplayValue;
        private ScreenRatio(String value, String displayValue) {
            mValue = value;
            mDisplayValue = displayValue;
        }
        public static ScreenRatio getEnum(String value) {
            for (ScreenRatio orient : values()) {
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
        public static int getIndex(ScreenRatio orientation) {
            int i = 0;
            for (ScreenRatio orient : values()) {
                if (orient == orientation) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static ScreenRatio getByIndex(int index) {
            int i = 0;
            for (ScreenRatio orient : values()) {
                if (i == index) {
                    return orient;
                }
                i++;
            }
            return null;
        }
    }
    public ScreenRatioQualifier() {
    }
    public ScreenRatioQualifier(ScreenRatio value) {
        mValue = value;
    }
    public ScreenRatio getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Ratio";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("ratio"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        ScreenRatio size = ScreenRatio.getEnum(value);
        if (size != null) {
            ScreenRatioQualifier qualifier = new ScreenRatioQualifier(size);
            config.setScreenRatioQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof ScreenRatioQualifier) {
            return mValue == ((ScreenRatioQualifier)qualifier).mValue;
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
