public final class Normalizer {
   private Normalizer() {};
    public static enum Form {
        NFD,
        NFC,
        NFKD,
        NFKC
    }
    public static String normalize(CharSequence src, Form form) {
        return NormalizerBase.normalize(src.toString(), form);
    }
    public static boolean isNormalized(CharSequence src, Form form) {
        return NormalizerBase.isNormalized(src.toString(), form);
    }
}
