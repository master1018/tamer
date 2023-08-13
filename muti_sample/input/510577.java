public class Typeface {
    public static final Typeface DEFAULT;
    public static final Typeface DEFAULT_BOLD;
    public static final Typeface SANS_SERIF;
    public static final Typeface SERIF;
    public static final Typeface MONOSPACE;
    private static Typeface[] sDefaults;
     int native_instance;
    public static final int NORMAL = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int BOLD_ITALIC = 3;
    public int getStyle() {
        return nativeGetStyle(native_instance);
    }
    public final boolean isBold() {
        return (getStyle() & BOLD) != 0;
    }
    public final boolean isItalic() {
        return (getStyle() & ITALIC) != 0;
    }
    public static Typeface create(String familyName, int style) {
        return new Typeface(nativeCreate(familyName, style));
    }
    public static Typeface create(Typeface family, int style) {
        int ni = 0;        
        if (family != null) {
            ni = family.native_instance;
        }
        return new Typeface(nativeCreateFromTypeface(ni, style));
    }
    public static Typeface defaultFromStyle(int style) {
        return sDefaults[style];
    }
    public static Typeface createFromAsset(AssetManager mgr, String path) {
        return new Typeface(nativeCreateFromAsset(mgr, path));
    }
    public static Typeface createFromFile(File path) {
        return new Typeface(nativeCreateFromFile(path.getAbsolutePath()));
    }
    public static Typeface createFromFile(String path) {
        return new Typeface(nativeCreateFromFile(path));
    }
    private Typeface(int ni) {
        if (0 == ni) {
            throw new RuntimeException("native typeface cannot be made");
        }
        native_instance = ni;
    }
    static {
        DEFAULT         = create((String)null, 0);
        DEFAULT_BOLD    = create((String)null, Typeface.BOLD);
        SANS_SERIF      = create("sans-serif", 0);
        SERIF           = create("serif", 0);
        MONOSPACE       = create("monospace", 0);
        sDefaults = new Typeface[] {
            DEFAULT,
            DEFAULT_BOLD,
            create((String)null, Typeface.ITALIC),
            create((String)null, Typeface.BOLD_ITALIC),
        };
    }
    protected void finalize() throws Throwable {
        super.finalize();
        nativeUnref(native_instance);
    }
    private static native int  nativeCreate(String familyName, int style);
    private static native int  nativeCreateFromTypeface(int native_instance, int style); 
    private static native void nativeUnref(int native_instance);
    private static native int  nativeGetStyle(int native_instance);
    private static native int  nativeCreateFromAsset(AssetManager mgr, String path);
    private static native int nativeCreateFromFile(String path);
    public static native void setGammaForText(float blackGamma, float whiteGamma);
}
