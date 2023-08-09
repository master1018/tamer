public final class NativeNormalizer {
    public static boolean isNormalized(CharSequence src, Form form) {
        return isNormalizedImpl(src.toString(), toUNormalizationMode(form));
    }
    public static String normalize(CharSequence src, Form form) {
        return normalizeImpl(src.toString(), toUNormalizationMode(form));
    }
    private static int toUNormalizationMode(Form form) {
        switch (form) {
        case NFC: return 4;
        case NFD: return 2;
        case NFKC: return 5;
        case NFKD: return 3;
        }
        throw new AssertionError("unknown Normalizer.Form " + form);
    }
    private static native String normalizeImpl(String src, int form);
    private static native boolean isNormalizedImpl(String src, int form);
    private NativeNormalizer() {}
}
