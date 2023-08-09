public abstract class CharsetDecoder {
    private static final int INIT = 0;
    private static final int ONGOING = 1;
    private static final int END = 2;
    private static final int FLUSH = 3;
    private float averChars;
    private float maxChars;
    private Charset cs;
    private CodingErrorAction malformAction;
    private CodingErrorAction unmapAction;
    private String replace;
    private int status;
    protected CharsetDecoder(Charset charset, float averageCharsPerByte,
            float maxCharsPerByte) {
        if (averageCharsPerByte <= 0 || maxCharsPerByte <= 0) {
            throw new IllegalArgumentException(Messages.getString("niochar.00")); 
        }
        if (averageCharsPerByte > maxCharsPerByte) {
            throw new IllegalArgumentException(Messages.getString("niochar.01")); 
        }
        averChars = averageCharsPerByte;
        maxChars = maxCharsPerByte;
        cs = charset;
        status = INIT;
        malformAction = CodingErrorAction.REPORT;
        unmapAction = CodingErrorAction.REPORT;
        replace = "\ufffd"; 
    }
    public final float averageCharsPerByte() {
        return averChars;
    }
    public final Charset charset() {
        return cs;
    }
    public final CharBuffer decode(ByteBuffer in)
            throws CharacterCodingException {
        reset();
        int length = (int) (in.remaining() * averChars);
        CharBuffer output = CharBuffer.allocate(length);
        CoderResult result = null;
        while (true) {
            result = decode(in, output, false);
            checkCoderResult(result);
            if (result.isUnderflow()) {
                break;
            } else if (result.isOverflow()) {
                output = allocateMore(output);
            }
        }
        result = decode(in, output, true);
        checkCoderResult(result);
        while (true) {
            result = flush(output);
            checkCoderResult(result);
            if (result.isOverflow()) {
                output = allocateMore(output);
            } else {
                break;
            }
        }
        output.flip();
        status = FLUSH;
        return output;
    }
    private void checkCoderResult(CoderResult result)
            throws CharacterCodingException {
        if (result.isMalformed() && malformAction == CodingErrorAction.REPORT) {
            throw new MalformedInputException(result.length());
        } else if (result.isUnmappable()
                && unmapAction == CodingErrorAction.REPORT) {
            throw new UnmappableCharacterException(result.length());
        }
    }
    private CharBuffer allocateMore(CharBuffer output) {
        if (output.capacity() == 0) {
            return CharBuffer.allocate(1);
        }
        CharBuffer result = CharBuffer.allocate(output.capacity() * 2);
        output.flip();
        result.put(output);
        return result;
    }
    public final CoderResult decode(ByteBuffer in, CharBuffer out,
            boolean endOfInput) {
        if ((status == FLUSH) || (!endOfInput && status == END)) {
            throw new IllegalStateException();
        }
        CoderResult result = null;
        while (true) {
            CodingErrorAction action = null;
            try {
                result = decodeLoop(in, out);
            } catch (BufferOverflowException ex) {
                throw new CoderMalfunctionError(ex);
            } catch (BufferUnderflowException ex) {
                throw new CoderMalfunctionError(ex);
            }
            if (result.isUnderflow()) {
                int remaining = in.remaining();
                status = endOfInput ? END : ONGOING;
                if (endOfInput && remaining > 0) {
                    result = CoderResult.malformedForLength(remaining);
                    in.position(in.position() + result.length());
                } else {
                    return result;
                }
            }
            if (result.isOverflow()) {
                return result;
            }
            action = malformAction;
            if (result.isUnmappable()) {
                action = unmapAction;
            }
            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replace.length()) {
                    return CoderResult.OVERFLOW;
                }
                out.put(replace);
            } else {
                if (action != CodingErrorAction.IGNORE)
                    return result;
            }
            if (!result.isMalformed()) {
                in.position(in.position() + result.length());
            }
        }
    }
    protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);
    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }
    public final CoderResult flush(CharBuffer out) {
        if (status != END && status != INIT) {
            throw new IllegalStateException();
        }
        CoderResult result = implFlush(out);
        if (result == CoderResult.UNDERFLOW) {
            status = FLUSH;
        }
        return result;
    }
    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }
    protected void implOnMalformedInput(CodingErrorAction newAction) {
    }
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
    }
    protected void implReplaceWith(String newReplacement) {
    }
    protected void implReset() {
    }
    public boolean isAutoDetecting() {
        return false;
    }
    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }
    public CodingErrorAction malformedInputAction() {
        return malformAction;
    }
    public final float maxCharsPerByte() {
        return maxChars;
    }
    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        malformAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }
    public final CharsetDecoder onUnmappableCharacter(
            CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        unmapAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }
    public final String replacement() {
        return replace;
    }
    public final CharsetDecoder replaceWith(String newReplacement) {
        if (null == newReplacement || newReplacement.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("niochar.06")); 
        }
        if (newReplacement.length() > maxChars) {
            throw new IllegalArgumentException(Messages.getString("niochar.07")); 
        }
        replace = newReplacement;
        implReplaceWith(newReplacement);
        return this;
    }
    public final CharsetDecoder reset() {
        status = INIT;
        implReset();
        return this;
    }
    public CodingErrorAction unmappableCharacterAction() {
        return unmapAction;
    }
}
