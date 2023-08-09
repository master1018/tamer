public class MockResources extends Resources {
    public MockResources() {
        super(new AssetManager(), null, null);
    }
    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
    }
    @Override
    public CharSequence getText(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getString(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public CharSequence getText(int id, CharSequence def) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public float getDimension(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public Movie getMovie(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int getColor(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int getInteger(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public DisplayMetrics getDisplayMetrics() {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getResourceName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
}
