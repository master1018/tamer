public class Surrogate {
    private Surrogate() { }
    public static final char MIN_HIGH = Character.MIN_HIGH_SURROGATE;
    public static final char MAX_HIGH = Character.MAX_HIGH_SURROGATE;
    public static final char MIN_LOW  = Character.MIN_LOW_SURROGATE;
    public static final char MAX_LOW  = Character.MAX_LOW_SURROGATE;
    public static final char MIN      = Character.MIN_SURROGATE;
    public static final char MAX      = Character.MAX_SURROGATE;
    public static final int UCS4_MIN  = Character.MIN_SUPPLEMENTARY_CODE_POINT;
    public static final int UCS4_MAX  = Character.MAX_CODE_POINT;
    public static boolean isHigh(int c) {
        return (MIN_HIGH <= c) && (c <= MAX_HIGH);
    }
    public static boolean isLow(int c) {
        return (MIN_LOW <= c) && (c <= MAX_LOW);
    }
    public static boolean is(int c) {
        return (MIN <= c) && (c <= MAX);
    }
    public static boolean neededFor(int uc) {
        return Character.isSupplementaryCodePoint(uc);
    }
    public static char high(int uc) {
        assert Character.isSupplementaryCodePoint(uc);
        return Character.highSurrogate(uc);
    }
    public static char low(int uc) {
        assert Character.isSupplementaryCodePoint(uc);
        return Character.lowSurrogate(uc);
    }
    public static int toUCS4(char c, char d) {
        assert Character.isHighSurrogate(c) && Character.isLowSurrogate(d);
        return Character.toCodePoint(c, d);
    }
    public static class Parser {
        public Parser() { }
        private int character;          
        private CoderResult error = CoderResult.UNDERFLOW;
        private boolean isPair;
        public int character() {
            assert (error == null);
            return character;
        }
        public boolean isPair() {
            assert (error == null);
            return isPair;
        }
        public int increment() {
            assert (error == null);
            return isPair ? 2 : 1;
        }
        public CoderResult error() {
            assert (error != null);
            return error;
        }
        public CoderResult unmappableResult() {
            assert (error == null);
            return CoderResult.unmappableForLength(isPair ? 2 : 1);
        }
        public int parse(char c, CharBuffer in) {
            if (Character.isHighSurrogate(c)) {
                if (!in.hasRemaining()) {
                    error = CoderResult.UNDERFLOW;
                    return -1;
                }
                char d = in.get();
                if (Character.isLowSurrogate(d)) {
                    character = Character.toCodePoint(c, d);
                    isPair = true;
                    error = null;
                    return character;
                }
                error = CoderResult.malformedForLength(1);
                return -1;
            }
            if (Character.isLowSurrogate(c)) {
                error = CoderResult.malformedForLength(1);
                return -1;
            }
            character = c;
            isPair = false;
            error = null;
            return character;
        }
        public int parse(char c, char[] ia, int ip, int il) {
            assert (ia[ip] == c);
            if (Character.isHighSurrogate(c)) {
                if (il - ip < 2) {
                    error = CoderResult.UNDERFLOW;
                    return -1;
                }
                char d = ia[ip + 1];
                if (Character.isLowSurrogate(d)) {
                    character = Character.toCodePoint(c, d);
                    isPair = true;
                    error = null;
                    return character;
                }
                error = CoderResult.malformedForLength(1);
                return -1;
            }
            if (Character.isLowSurrogate(c)) {
                error = CoderResult.malformedForLength(1);
                return -1;
            }
            character = c;
            isPair = false;
            error = null;
            return character;
        }
    }
    public static class Generator {
        public Generator() { }
        private CoderResult error = CoderResult.OVERFLOW;
        public CoderResult error() {
            assert error != null;
            return error;
        }
        public int generate(int uc, int len, CharBuffer dst) {
            if (Character.isBmpCodePoint(uc)) {
                char c = (char) uc;
                if (Character.isSurrogate(c)) {
                    error = CoderResult.malformedForLength(len);
                    return -1;
                }
                if (dst.remaining() < 1) {
                    error = CoderResult.OVERFLOW;
                    return -1;
                }
                dst.put(c);
                error = null;
                return 1;
            } else if (Character.isValidCodePoint(uc)) {
                if (dst.remaining() < 2) {
                    error = CoderResult.OVERFLOW;
                    return -1;
                }
                dst.put(Character.highSurrogate(uc));
                dst.put(Character.lowSurrogate(uc));
                error = null;
                return 2;
            } else {
                error = CoderResult.unmappableForLength(len);
                return -1;
            }
        }
        public int generate(int uc, int len, char[] da, int dp, int dl) {
            if (Character.isBmpCodePoint(uc)) {
                char c = (char) uc;
                if (Character.isSurrogate(c)) {
                    error = CoderResult.malformedForLength(len);
                    return -1;
                }
                if (dl - dp < 1) {
                    error = CoderResult.OVERFLOW;
                    return -1;
                }
                da[dp] = c;
                error = null;
                return 1;
            } else if (Character.isValidCodePoint(uc)) {
                if (dl - dp < 2) {
                    error = CoderResult.OVERFLOW;
                    return -1;
                }
                da[dp] = Character.highSurrogate(uc);
                da[dp + 1] = Character.lowSurrogate(uc);
                error = null;
                return 2;
            } else {
                error = CoderResult.unmappableForLength(len);
                return -1;
            }
        }
    }
}
