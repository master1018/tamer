public class HideReturnsTransformationMethod
extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = new char[] { '\r' };
    private static char[] REPLACEMENT = new char[] { '\uFEFF' };
    protected char[] getOriginal() {
        return ORIGINAL;
    }
    protected char[] getReplacement() {
        return REPLACEMENT;
    }
    public static HideReturnsTransformationMethod getInstance() {
        if (sInstance != null)
            return sInstance;
        sInstance = new HideReturnsTransformationMethod();
        return sInstance;
    }
    private static HideReturnsTransformationMethod sInstance;
}
