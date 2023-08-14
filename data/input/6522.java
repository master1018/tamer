public final class JCAUtil {
    private JCAUtil() {
    }
    private static final Object LOCK = JCAUtil.class;
    private static volatile SecureRandom secureRandom;
    private final static int ARRAY_SIZE = 4096;
    public static int getTempArraySize(int totalSize) {
        return Math.min(ARRAY_SIZE, totalSize);
    }
    public static SecureRandom getSecureRandom() {
        SecureRandom r = secureRandom;
        if (r == null) {
            synchronized (LOCK) {
                r = secureRandom;
                if (r == null) {
                    r = new SecureRandom();
                    secureRandom = r;
                }
            }
        }
        return r;
    }
}
