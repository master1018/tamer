public final class NavigationMethodQualifier extends ResourceQualifier {
    public static final String NAME = "Navigation Method";
    private NavigationMethod mValue;
    public static enum NavigationMethod {
        DPAD("dpad", "D-pad"), 
        TRACKBALL("trackball", "Trackball"), 
        WHEEL("wheel", "Wheel"), 
        NONAV("nonav", "No Navigation"); 
        private String mValue;
        private String mDisplay;
        private NavigationMethod(String value, String display) {
            mValue = value;
            mDisplay = display;
        }
        public static NavigationMethod getEnum(String value) {
            for (NavigationMethod orient : values()) {
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
            return mDisplay;
        }
        public static int getIndex(NavigationMethod value) {
            int i = 0;
            for (NavigationMethod nav : values()) {
                if (nav == value) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        public static NavigationMethod getByIndex(int index) {
            int i = 0;
            for (NavigationMethod value : values()) {
                if (i == index) {
                    return value;
                }
                i++;
            }
            return null;
        }
    }
    public NavigationMethodQualifier() {
    }
    public NavigationMethodQualifier(NavigationMethod value) {
        mValue = value;
    }
    public NavigationMethod getValue() {
        return mValue;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Navigation";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("navpad"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        NavigationMethod method = NavigationMethod.getEnum(value);
        if (method != null) {
            NavigationMethodQualifier qualifier = new NavigationMethodQualifier(method);
            config.setNavigationMethodQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof NavigationMethodQualifier) {
            return mValue == ((NavigationMethodQualifier)qualifier).mValue;
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
