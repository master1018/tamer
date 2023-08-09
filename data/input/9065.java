public class COMPOUND_TEXT_Encoder extends CharsetEncoder {
    private static final Map<String,CharsetEncoder> encodingToEncoderMap =
      Collections.synchronizedMap(new HashMap<String,CharsetEncoder>(21, 1.0f));
    private static final CharsetEncoder latin1Encoder;
    private static final CharsetEncoder defaultEncoder;
    private static final boolean defaultEncodingSupported;
    static {
        CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
        String encoding = encoder.charset().name();
        if ("ISO8859_1".equals(encoding)) {
            latin1Encoder = encoder;
            defaultEncoder = encoder;
            defaultEncodingSupported = true;
        } else {
            try {
                latin1Encoder =
                    Charset.forName("ISO8859_1").newEncoder();
            } catch (IllegalArgumentException e) {
                throw new ExceptionInInitializerError
                    ("ISO8859_1 unsupported");
            }
            defaultEncoder = encoder;
            defaultEncodingSupported = CompoundTextSupport.getEncodings().
                contains(defaultEncoder.charset().name());
        }
    }
    private CharsetEncoder encoder;
    private char[] charBuf = new char[1];
    private CharBuffer charbuf = CharBuffer.wrap(charBuf);
    private ByteArrayOutputStream nonStandardCharsetBuffer;
    private byte[] byteBuf;
    private ByteBuffer bytebuf;
    private int numNonStandardChars, nonStandardEncodingLen;
    public COMPOUND_TEXT_Encoder(Charset cs) {
        super(cs,
              (float)(CompoundTextSupport.MAX_CONTROL_SEQUENCE_LEN + 2),
              (float)(CompoundTextSupport.MAX_CONTROL_SEQUENCE_LEN + 2));
        try {
            encoder = Charset.forName("ISO8859_1").newEncoder();
        } catch (IllegalArgumentException cannotHappen) {}
        initEncoder(encoder);
    }
    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer des) {
        CoderResult cr = CoderResult.UNDERFLOW;
        char[] input = src.array();
        int inOff = src.arrayOffset() + src.position();
        int inEnd = src.arrayOffset() + src.limit();
        try {
            while (inOff < inEnd && cr.isUnderflow()) {
                charBuf[0] = input[inOff];
                if (charBuf[0] <= '\u0008' ||
                    (charBuf[0] >= '\u000B' && charBuf[0] <= '\u001F') ||
                    (charBuf[0] >= '\u0080' && charBuf[0] <= '\u009F')) {
                    charBuf[0] = '?';
                }
                CharsetEncoder enc = getEncoder(charBuf[0]);
                if (enc == null) {
                    if (unmappableCharacterAction()
                        == CodingErrorAction.REPORT) {
                        charBuf[0] = '?';
                        enc = latin1Encoder;
                    } else {
                        return CoderResult.unmappableForLength(1);
                    }
                }
                if (enc != encoder) {
                    if (nonStandardCharsetBuffer != null) {
                        cr = flushNonStandardCharsetBuffer(des);
                    } else {
                        flushEncoder(encoder, des);
                    }
                    if (!cr.isUnderflow())
                        return cr;
                    byte[] escSequence = CompoundTextSupport.
                        getEscapeSequence(enc.charset().name());
                    if (escSequence == null) {
                        throw new InternalError("Unknown encoding: " +
                                                enc.charset().name());
                    } else if (escSequence[1] == (byte)0x25 &&
                               escSequence[2] == (byte)0x2F) {
                        initNonStandardCharsetBuffer(enc, escSequence);
                    } else if (des.remaining() >= escSequence.length) {
                        des.put(escSequence, 0, escSequence.length);
                    } else {
                        return CoderResult.OVERFLOW;
                    }
                    encoder = enc;
                    continue;
                }
                charbuf.rewind();
                if (nonStandardCharsetBuffer == null) {
                    cr = encoder.encode(charbuf, des, false);
                } else {
                    bytebuf.clear();
                    cr = encoder.encode(charbuf, bytebuf, false);
                    bytebuf.flip();
                    nonStandardCharsetBuffer.write(byteBuf,
                                                   0, bytebuf.limit());
                    numNonStandardChars++;
                }
                inOff++;
            }
            return cr;
        } finally {
            src.position(inOff - src.arrayOffset());
        }
    }
    protected CoderResult implFlush(ByteBuffer out) {
        CoderResult cr = (nonStandardCharsetBuffer != null)
            ? flushNonStandardCharsetBuffer(out)
            : flushEncoder(encoder, out);
        reset();
        return cr;
    }
    private void initNonStandardCharsetBuffer(CharsetEncoder c,
                                              byte[] escSequence)
    {
        nonStandardCharsetBuffer = new ByteArrayOutputStream();
        byteBuf = new byte[(int)c.maxBytesPerChar()];
        bytebuf = ByteBuffer.wrap(byteBuf);
        nonStandardCharsetBuffer.write(escSequence, 0, escSequence.length);
        nonStandardCharsetBuffer.write(0); 
        nonStandardCharsetBuffer.write(0); 
        byte[] encoding = CompoundTextSupport.
            getEncoding(c.charset().name());
        if (encoding == null) {
            throw new InternalError
                ("Unknown encoding: " + encoder.charset().name());
        }
        nonStandardCharsetBuffer.write(encoding, 0, encoding.length);
        nonStandardCharsetBuffer.write(0x02); 
        nonStandardEncodingLen = encoding.length + 1;
    }
    private CoderResult flushNonStandardCharsetBuffer(ByteBuffer out) {
        if (numNonStandardChars > 0) {
            byte[] flushBuf = new byte[(int)encoder.maxBytesPerChar() *
                                       numNonStandardChars];
            ByteBuffer bb = ByteBuffer.wrap(flushBuf);
            flushEncoder(encoder, bb);
            bb.flip();
            nonStandardCharsetBuffer.write(flushBuf, 0, bb.limit());
            numNonStandardChars = 0;
        }
        int numBytes = nonStandardCharsetBuffer.size();
        int nonStandardBytesOff = 6 + nonStandardEncodingLen;
        if (out.remaining() < (numBytes - nonStandardBytesOff) +
            nonStandardBytesOff * (((numBytes - nonStandardBytesOff) /
                                    ((1 << 14) - 1)) + 1))
        {
            return CoderResult.OVERFLOW;
        }
        byte[] nonStandardBytes =
            nonStandardCharsetBuffer.toByteArray();
        do {
            out.put((byte)0x1B);
            out.put((byte)0x25);
            out.put((byte)0x2F);
            out.put(nonStandardBytes[3]);
            int toWrite = Math.min(numBytes - nonStandardBytesOff,
                                   (1 << 14) - 1 - nonStandardEncodingLen);
            out.put((byte)
                (((toWrite + nonStandardEncodingLen) / 0x80) | 0x80)); 
            out.put((byte)
                (((toWrite + nonStandardEncodingLen) % 0x80) | 0x80)); 
            out.put(nonStandardBytes, 6, nonStandardEncodingLen);
            out.put(nonStandardBytes, nonStandardBytesOff, toWrite);
            nonStandardBytesOff += toWrite;
        } while (nonStandardBytesOff < numBytes);
        nonStandardCharsetBuffer = null;
        byteBuf = null;
        nonStandardEncodingLen = 0;
        return CoderResult.UNDERFLOW;
    }
    protected void implReset() {
        numNonStandardChars = nonStandardEncodingLen = 0;
        nonStandardCharsetBuffer = null;
        byteBuf = null;
        try {
            encoder = Charset.forName("ISO8859_1").newEncoder();
        } catch (IllegalArgumentException cannotHappen) {
        }
        initEncoder(encoder);
    }
    public boolean canEncode(char ch) {
        return getEncoder(ch) != null;
    }
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        encoder.onUnmappableCharacter(newAction);
    }
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        encoder.onUnmappableCharacter(newAction);
    }
    protected void implReplaceWith(byte[] newReplacement) {
        if (encoder != null)
            encoder.replaceWith(newReplacement);
    }
    private CharsetEncoder getEncoder(char ch) {
        if (encoder.canEncode(ch)) {
            return encoder;
        }
        if (defaultEncodingSupported && defaultEncoder.canEncode(ch)) {
            CharsetEncoder retval = null;
            try {
                retval = defaultEncoder.charset().newEncoder();
            } catch (UnsupportedOperationException cannotHappen) {
            }
            initEncoder(retval);
            return retval;
        }
        if (latin1Encoder.canEncode(ch)) {
            CharsetEncoder retval = null;
            try {
                retval = latin1Encoder.charset().newEncoder();
            } catch (UnsupportedOperationException cannotHappen) {}
            initEncoder(retval);
            return retval;
        }
        for (String encoding : CompoundTextSupport.getEncodings())
        {
            CharsetEncoder enc = encodingToEncoderMap.get(encoding);
            if (enc == null) {
                enc = CompoundTextSupport.getEncoder(encoding);
                if (enc == null) {
                    throw new InternalError("Unsupported encoding: " +
                                            encoding);
                }
                encodingToEncoderMap.put(encoding, enc);
            }
            if (enc.canEncode(ch)) {
                CharsetEncoder retval = CompoundTextSupport.getEncoder(encoding);
                initEncoder(retval);
                return retval;
            }
        }
        return null;
    }
    private void initEncoder(CharsetEncoder enc) {
        try {
            enc.onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith(replacement());
        } catch (IllegalArgumentException x) {}
    }
    private CharBuffer fcb= CharBuffer.allocate(0);
    private CoderResult flushEncoder(CharsetEncoder enc, ByteBuffer bb) {
        enc.encode(fcb, bb, true);
        return enc.flush(bb);
    }
}
