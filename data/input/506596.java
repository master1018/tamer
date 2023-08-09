public class CoderResult {
    private static final int TYPE_UNDERFLOW = 1;
    private static final int TYPE_OVERFLOW = 2;
    private static final int TYPE_MALFORMED_INPUT = 3;
    private static final int TYPE_UNMAPPABLE_CHAR = 4;
    public static final CoderResult UNDERFLOW = new CoderResult(TYPE_UNDERFLOW,
            0);
    public static final CoderResult OVERFLOW = new CoderResult(TYPE_OVERFLOW, 0);
    private static WeakHashMap<Integer, CoderResult> _malformedErrors = new WeakHashMap<Integer, CoderResult>();
    private static WeakHashMap<Integer, CoderResult> _unmappableErrors = new WeakHashMap<Integer, CoderResult>();
    private final int type;
    private final int length;
    private CoderResult(int type, int length) {
        super();
        this.type = type;
        this.length = length;
    }
    public static synchronized CoderResult malformedForLength(int length)
            throws IllegalArgumentException {
        if (length > 0) {
            Integer key = Integer.valueOf(length);
            synchronized (_malformedErrors) {
                CoderResult r = _malformedErrors.get(key);
                if (null == r) {
                    r = new CoderResult(TYPE_MALFORMED_INPUT, length);
                    _malformedErrors.put(key, r);
                }
                return r;
            }
        }
        throw new IllegalArgumentException(Messages.getString(
                "niochar.08", length)); 
    }
    public static synchronized CoderResult unmappableForLength(int length)
            throws IllegalArgumentException {
        if (length > 0) {
            Integer key = Integer.valueOf(length);
            synchronized (_unmappableErrors) {
                CoderResult r = _unmappableErrors.get(key);
                if (null == r) {
                    r = new CoderResult(TYPE_UNMAPPABLE_CHAR, length);
                    _unmappableErrors.put(key, r);
                }
                return r;
            }
        }
        throw new IllegalArgumentException(Messages.getString(
                "niochar.08", length)); 
    }
    public boolean isUnderflow() {
        return this.type == TYPE_UNDERFLOW;
    }
    public boolean isError() {
        return this.type == TYPE_MALFORMED_INPUT
                || this.type == TYPE_UNMAPPABLE_CHAR;
    }
    public boolean isMalformed() {
        return this.type == TYPE_MALFORMED_INPUT;
    }
    public boolean isOverflow() {
        return this.type == TYPE_OVERFLOW;
    }
    public boolean isUnmappable() {
        return this.type == TYPE_UNMAPPABLE_CHAR;
    }
    public int length() throws UnsupportedOperationException {
        if (this.type == TYPE_MALFORMED_INPUT
                || this.type == TYPE_UNMAPPABLE_CHAR) {
            return this.length;
        }
        throw new UnsupportedOperationException(Messages
                .getString("niochar.09")); 
    }
    public void throwException() throws BufferUnderflowException,
            BufferOverflowException, UnmappableCharacterException,
            MalformedInputException, CharacterCodingException {
        switch (this.type) {
            case TYPE_UNDERFLOW:
                throw new BufferUnderflowException();
            case TYPE_OVERFLOW:
                throw new BufferOverflowException();
            case TYPE_UNMAPPABLE_CHAR:
                throw new UnmappableCharacterException(this.length);
            case TYPE_MALFORMED_INPUT:
                throw new MalformedInputException(this.length);
            default:
                throw new CharacterCodingException();
        }
    }
    @Override
    public String toString() {
        String dsc = null;
        switch (this.type) {
            case TYPE_UNDERFLOW:
                dsc = "UNDERFLOW error"; 
                break;
            case TYPE_OVERFLOW:
                dsc = "OVERFLOW error"; 
                break;
            case TYPE_UNMAPPABLE_CHAR:
                dsc = "Unmappable-character error with erroneous input length " 
                        + this.length;
                break;
            case TYPE_MALFORMED_INPUT:
                dsc = "Malformed-input error with erroneous input length " 
                        + this.length;
                break;
            default:
                dsc = ""; 
                break;
        }
        return "CoderResult[" + dsc + "]"; 
    }
}
