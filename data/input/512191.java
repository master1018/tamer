public final class CharsetEncoderICU extends CharsetEncoder {
    private static final int INPUT_OFFSET = 0,
                             OUTPUT_OFFSET = 1,
                             INVALID_CHARS  = 2,
                             INPUT_HELD     = 3,
                             LIMIT          = 4;
    private int[] data = new int[LIMIT];
    private long converterHandle=0;
    private char[] input = null;
    private byte[] output = null;
    private char[] allocatedInput = null;
    private byte[] allocatedOutput = null;
    private int inEnd;
    private int outEnd;
    private int ec;
    private int savedInputHeldLen;
    private int onUnmappableInput = NativeConverter.STOP_CALLBACK;;
    private int onMalformedInput = NativeConverter.STOP_CALLBACK;;
    public CharsetEncoderICU(Charset cs, long cHandle, byte[] replacement) {
        super(
            cs,
            (float) NativeConverter.getAveBytesPerChar(cHandle),
            (float) NativeConverter.getMaxBytesPerChar(cHandle),
            replacement);
        byte[] sub = replacement();
        ec = NativeConverter.setCallbackEncode( cHandle,
                                                onMalformedInput,
                                                onUnmappableInput,
                                                sub, sub.length);
        converterHandle = cHandle;
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }
    }
    protected void implReplaceWith(byte[] newReplacement) {
        if (converterHandle != 0) {
            if (newReplacement.length
                > NativeConverter.getMaxBytesPerChar(converterHandle)) {
                throw new IllegalArgumentException("Number of replacement Bytes are greater than max bytes per char");
            }
            ec = NativeConverter.setSubstitutionBytes(converterHandle,
                                                      newReplacement,
                                                      newReplacement.length);
            if (ErrorCode.isFailure(ec)) {
                throw ErrorCode.getException(ec);
            }
        }
    }
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        onMalformedInput = NativeConverter.STOP_CALLBACK;
        if (newAction.equals(CodingErrorAction.IGNORE)) {
            onMalformedInput = NativeConverter.SKIP_CALLBACK;
        } else if (newAction.equals(CodingErrorAction.REPLACE)) {
            onMalformedInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }
        byte[] sub = replacement();
        ec = NativeConverter.setCallbackEncode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }
    }
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        onUnmappableInput = NativeConverter.STOP_CALLBACK;
        if (newAction.equals(CodingErrorAction.IGNORE)) {
            onUnmappableInput = NativeConverter.SKIP_CALLBACK;
        } else if (newAction.equals(CodingErrorAction.REPLACE)) {
            onUnmappableInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }
        byte[] sub = replacement();
        ec = NativeConverter.setCallbackEncode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }
    }
    protected CoderResult implFlush(ByteBuffer out) {
        try {
            data[OUTPUT_OFFSET] = getArray(out);
            ec = NativeConverter.flushCharToByte(converterHandle,
                                                 output, 
                                                 outEnd, 
                                                 data 
                                                );
            if (ErrorCode.isFailure(ec)) {
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                }else if (ec == ErrorCode.U_TRUNCATED_CHAR_FOUND) {
                    if(data[INPUT_OFFSET]>0){
                        return CoderResult.malformedForLength(data[INPUT_OFFSET]);
                    }
                }else {
                    ErrorCode.getException(ec);
                }
            }
            return CoderResult.UNDERFLOW;
        } finally {
            setPosition(out);
            implReset();
        }
    }
    protected void implReset() {
        NativeConverter.resetCharToByte(converterHandle);
        data[INPUT_OFFSET] = 0;
        data[OUTPUT_OFFSET] = 0;
        data[INVALID_CHARS] = 0;
        data[INPUT_HELD] = 0;
        savedInputHeldLen = 0;
    }
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        if (!in.hasRemaining()) {
            return CoderResult.UNDERFLOW;
        }
        data[INPUT_OFFSET] = getArray(in);
        data[OUTPUT_OFFSET]= getArray(out);
        data[INPUT_HELD] = 0;
        data[INVALID_CHARS] = 0; 
        try {
            ec = NativeConverter.encode(converterHandle,
                                        input, 
                                        inEnd, 
                                        output, 
                                        outEnd, 
                                        data, 
                                        false 
                                        );
            if (ErrorCode.isFailure(ec)) {
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                } else if (ec == ErrorCode.U_INVALID_CHAR_FOUND) {
                    return CoderResult.unmappableForLength(data[INVALID_CHARS]);
                } else if (ec == ErrorCode.U_ILLEGAL_CHAR_FOUND) {
                    return CoderResult.malformedForLength(data[INVALID_CHARS]);
                }
            }
            return CoderResult.UNDERFLOW;
        } finally {
            setPosition(in);
            setPosition(out);
        }
    }
    public boolean canEncode(char c) {
        return canEncode((int) c);
    }
    public boolean canEncode(int codepoint) {
        return NativeConverter.canEncode(converterHandle, codepoint);
    }
    protected void finalize() throws Throwable {
        NativeConverter.closeConverter(converterHandle);
        super.finalize();
        converterHandle=0;
    }
    private final int getArray(ByteBuffer out) {
        if(out.hasArray()){
            output = out.array();
            outEnd = out.arrayOffset() + out.limit();
            return out.arrayOffset() + out.position();
        }else{
            outEnd = out.remaining();
            if (allocatedOutput == null || (outEnd > allocatedOutput.length)) {
                allocatedOutput = new byte[outEnd];
            }
            output = allocatedOutput;
            return 0;
        }
    }
    private final int getArray(CharBuffer in) {
        if(in.hasArray()){
            input = in.array();
            inEnd = in.arrayOffset() + in.limit();
            return in.arrayOffset() + in.position() + savedInputHeldLen;
        }else{
            inEnd = in.remaining();
            if (allocatedInput == null || (inEnd > allocatedInput.length)) {
                allocatedInput = new char[inEnd];
            }
            input = allocatedInput;
            int pos = in.position();
            in.get(input,0,inEnd);
            in.position(pos);
            return savedInputHeldLen;
        }
    }
    private final void setPosition(ByteBuffer out) {
        if (out.hasArray()) {
            out.position(out.position() + data[OUTPUT_OFFSET] - out.arrayOffset());
        } else {
            out.put(output, 0, data[OUTPUT_OFFSET]);
        }
        output = null;
    }
    private final void setPosition(CharBuffer in){
        int len = in.position() + data[INPUT_OFFSET] + savedInputHeldLen;
        len -= data[INVALID_CHARS]; 
        in.position(len);   
        savedInputHeldLen = data[INPUT_HELD];
        if(!(data[OUTPUT_OFFSET]>0 && savedInputHeldLen>0)){
            in.position(in.position() - savedInputHeldLen);
        }     
        input = null;
    }
}
