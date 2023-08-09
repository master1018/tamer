public class SingleLineTransformationMethod
extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = new char[] { '\n', '\r' };
    private static char[] REPLACEMENT = new char[] { ' ', '\uFEFF' };
    protected char[] getOriginal() {
        return ORIGINAL;
    }
    protected char[] getReplacement() {
        return REPLACEMENT;
    }
    public static SingleLineTransformationMethod getInstance() {
        if (sInstance != null)
            return sInstance;
        sInstance = new SingleLineTransformationMethod();
        return sInstance;
    }
    private static SingleLineTransformationMethod sInstance;
}
