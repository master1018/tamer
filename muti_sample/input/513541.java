public abstract class CharsetEncoder {
    private static final int INIT = 0;
    private static final int ONGOING = 1;
    private static final int END = 2;
    private static final int FLUSH = 3;
    private Charset cs;
    private float averBytes;
    private float maxBytes;
    private byte[] replace;
    private int status;
    private CodingErrorAction malformAction;
    private CodingErrorAction unmapAction;
    private CharsetDecoder decoder;
    protected CharsetEncoder(Charset cs, float averageBytesPerChar,
            float maxBytesPerChar) {
        this(cs, averageBytesPerChar, maxBytesPerChar,
                new byte[] { (byte) '?' });
    }
    protected CharsetEncoder(Charset cs, float averageBytesPerChar,
            float maxBytesPerChar, byte[] replacement) {
        if (averageBytesPerChar <= 0 || maxBytesPerChar <= 0) {
            throw new IllegalArgumentException(Messages.getString("niochar.02")); 
        }
        if (averageBytesPerChar > maxBytesPerChar) {
            throw new IllegalArgumentException(Messages.getString("niochar.03")); 
        }
        this.cs = cs;
        averBytes = averageBytesPerChar;
        maxBytes = maxBytesPerChar;
        status = INIT;
        malformAction = CodingErrorAction.REPORT;
        unmapAction = CodingErrorAction.REPORT;
        replaceWith(replacement);
    }
    public final float averageBytesPerChar() {
        return averBytes;
    }
    public boolean canEncode(char c) {
        return implCanEncode(CharBuffer.wrap(new char[] { c }));
    }
    private boolean implCanEncode(CharBuffer cb) {
        if (status == FLUSH) {
            status = INIT;
        }
        if (status != INIT) {
            throw new IllegalStateException(Messages.getString("niochar.0B")); 
        }
        CodingErrorAction malformBak = malformAction;
        CodingErrorAction unmapBak = unmapAction;
        onMalformedInput(CodingErrorAction.REPORT);
        onUnmappableCharacter(CodingErrorAction.REPORT);
        boolean result = true;
        try {
            this.encode(cb);
        } catch (CharacterCodingException e) {
            result = false;
        }
        onMalformedInput(malformBak);
        onUnmappableCharacter(unmapBak);
        reset();
        return result;
    }
    public boolean canEncode(CharSequence sequence) {
        CharBuffer cb;
        if (sequence instanceof CharBuffer) {
            cb = ((CharBuffer) sequence).duplicate();
        } else {
            cb = CharBuffer.wrap(sequence);
        }
        return implCanEncode(cb);
    }
    public final Charset charset() {
        return cs;
    }
    public final ByteBuffer encode(CharBuffer in)
            throws CharacterCodingException {
        if (in.remaining() == 0) {
            return ByteBuffer.allocate(0);
        }
        reset();
        int length = (int) (in.remaining() * averBytes);
        ByteBuffer output = ByteBuffer.allocate(length);
        CoderResult result = null;
        while (true) {
            result = encode(in, output, false);
            if (result==CoderResult.UNDERFLOW) {
                break;
            } else if (result==CoderResult.OVERFLOW) {
                output = allocateMore(output);
                continue;
            }
            checkCoderResult(result);
        }
        result = encode(in, output, true);
        checkCoderResult(result);
        while (true) {
            result = flush(output);
            if (result==CoderResult.UNDERFLOW) {
                output.flip();
                break;
            } else if (result==CoderResult.OVERFLOW) {
                output = allocateMore(output);
                continue;
            }
            checkCoderResult(result);
            output.flip();
            if (result.isMalformed()) {
                throw new MalformedInputException(result.length());
            } else if (result.isUnmappable()) {
                throw new UnmappableCharacterException(result.length());
            }
            break;
        }
        status = FLUSH;
        return output;
    }
    private void checkCoderResult(CoderResult result)
            throws CharacterCodingException {
        if (malformAction == CodingErrorAction.REPORT && result.isMalformed() ) {
            throw new MalformedInputException(result.length());
        } else if (unmapAction == CodingErrorAction.REPORT && result.isUnmappable()) {
            throw new UnmappableCharacterException(result.length());
        }
    }
    private ByteBuffer allocateMore(ByteBuffer output) {
        if (output.capacity() == 0) {
            return ByteBuffer.allocate(1);
        }
        ByteBuffer result = ByteBuffer.allocate(output.capacity() * 2);
        output.flip();
        result.put(output);
        return result;
    }
    public final CoderResult encode(CharBuffer in, ByteBuffer out,
            boolean endOfInput) {
        if ((status == FLUSH) || (!endOfInput && status == END)) {
            throw new IllegalStateException();
        }
        CoderResult result;
        while (true) {
            try {
                result = encodeLoop(in, out);
            } catch (BufferOverflowException e) {
                throw new CoderMalfunctionError(e);
            } catch (BufferUnderflowException e) {
                throw new CoderMalfunctionError(e);
            }
            if (result==CoderResult.UNDERFLOW) {
                status = endOfInput ? END : ONGOING;
                if (endOfInput) {
                    int remaining = in.remaining();
                    if (remaining > 0) {
                        result = CoderResult.malformedForLength(remaining);
                    } else {
                        return result;
                    }
                } else {
                    return result;
                }
            } else if (result==CoderResult.OVERFLOW) {
                status = endOfInput ? END : ONGOING;
                return result;
            }
            CodingErrorAction action = malformAction;
            if (result.isUnmappable()) {
                action = unmapAction;
            }
            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replace.length) {
                    return CoderResult.OVERFLOW;
                }
                out.put(replace);
            } else {
                if (action != CodingErrorAction.IGNORE) {
                    return result;
                }
            }
            in.position(in.position() + result.length());
        }
    }
    protected abstract CoderResult encodeLoop(CharBuffer in, ByteBuffer out);
    public final CoderResult flush(ByteBuffer out) {
        if (status != END && status != INIT) {
            throw new IllegalStateException();
        }
        CoderResult result = implFlush(out);
        if (result == CoderResult.UNDERFLOW) {
            status = FLUSH;
        }
        return result;
    }
    protected CoderResult implFlush(ByteBuffer out) {
        return CoderResult.UNDERFLOW;
    }
    protected void implOnMalformedInput(CodingErrorAction newAction) {
    }
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
    }
    protected void implReplaceWith(byte[] newReplacement) {
    }
    protected void implReset() {
    }
    public boolean isLegalReplacement(byte[] repl) {
        if (decoder == null) {
            decoder = cs.newDecoder();
        }
        CodingErrorAction malform = decoder.malformedInputAction();
        CodingErrorAction unmap = decoder.unmappableCharacterAction();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer in = ByteBuffer.wrap(repl);
        CharBuffer out = CharBuffer.allocate((int) (repl.length * decoder
                .maxCharsPerByte()));
        CoderResult result = decoder.decode(in, out, true);
        decoder.onMalformedInput(malform);
        decoder.onUnmappableCharacter(unmap);
        return !result.isError();
    }
    public CodingErrorAction malformedInputAction() {
        return malformAction;
    }
    public final float maxBytesPerChar() {
        return maxBytes;
    }
    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException(Messages.getString("niochar.0C")); 
        }
        malformAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }
    public final CharsetEncoder onUnmappableCharacter(
            CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException(Messages.getString("niochar.0D")); 
        }
        unmapAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }
    public final byte[] replacement() {
        return replace;
    }
    public final CharsetEncoder replaceWith(byte[] replacement) {
        if (null == replacement || 0 == replacement.length
                || maxBytes < replacement.length
                || !isLegalReplacement(replacement)) {
            throw new IllegalArgumentException(Messages.getString("niochar.0E")); 
        }
        replace = replacement;
        implReplaceWith(replacement);
        return this;
    }
    public final CharsetEncoder reset() {
        status = INIT;
        implReset();
        return this;
    }
    public CodingErrorAction unmappableCharacterAction() {
        return unmapAction;
    }
}
