public final class ScreenDimensionQualifier extends ResourceQualifier {
    final static int DEFAULT_SIZE = -1;
    private final static Pattern sDimensionPattern = Pattern.compile(
            "^(\\d+)x(\\d+)$"); 
    public static final String NAME = "Screen Dimension";
    private int mValue1 = DEFAULT_SIZE;
    private int mValue2 = DEFAULT_SIZE;
    public ScreenDimensionQualifier() {
    }
    public ScreenDimensionQualifier(int value1, int value2) {
        mValue1 = value1;
        mValue2 = value2;
    }
    public int getValue1() {
        return mValue1;
    }
    public int getValue2() {
        return mValue2;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Dimension";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("dimension"); 
    }
    @Override
    public boolean isValid() {
        return mValue1 != DEFAULT_SIZE && mValue2 != DEFAULT_SIZE;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        Matcher m = sDimensionPattern.matcher(value);
        if (m.matches()) {
            String d1 = m.group(1);
            String d2 = m.group(2);
            ScreenDimensionQualifier qualifier = getQualifier(d1, d2);
            if (qualifier != null) {
                config.setScreenDimensionQualifier(qualifier);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof ScreenDimensionQualifier) {
            ScreenDimensionQualifier q = (ScreenDimensionQualifier)qualifier;
            return (mValue1 == q.mValue1 && mValue2 == q.mValue2);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    public static ScreenDimensionQualifier getQualifier(String size1, String size2) {
        try {
            int s1 = Integer.parseInt(size1);
            int s2 = Integer.parseInt(size2);
            ScreenDimensionQualifier qualifier = new ScreenDimensionQualifier();
            if (s1 > s2) {
                qualifier.mValue1 = s1;
                qualifier.mValue2 = s2;
            } else {
                qualifier.mValue1 = s2;
                qualifier.mValue2 = s1;
            }
            return qualifier;
        } catch (NumberFormatException e) {
        }
        return null;
    }
    @Override
    public String getFolderSegment(IAndroidTarget target) {
        return String.format("%1$dx%2$d", mValue1, mValue2); 
    }
    @Override
    public String getStringValue() {
        if (mValue1 != -1 && mValue2 != -1) {
            return String.format("%1$dx%2$d", mValue1, mValue2);
        }
        return ""; 
    }
}
