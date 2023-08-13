public final class CharsetDecoderICU extends CharsetDecoder{ 
    private static final int INPUT_OFFSET   = 0,
                             OUTPUT_OFFSET  = 1,
                             INVALID_BYTES  = 2,
                             INPUT_HELD     = 3,
                             LIMIT          = 4;
    private int[] data = new int[LIMIT];
    private long converterHandle=0;
    private  byte[] input = null;
    private  char[] output= null;
    private byte[] allocatedInput = null;
    private char[] allocatedOutput = null;
    private int inEnd;
    private int outEnd;
    private int ec;
    private int onUnmappableInput = NativeConverter.STOP_CALLBACK;;
    private int onMalformedInput = NativeConverter.STOP_CALLBACK;;
    private int savedInputHeldLen;
    public CharsetDecoderICU(Charset cs,long cHandle){
         super(cs,
               NativeConverter.getAveCharsPerByte(cHandle),
               NativeConverter.getMaxCharsPerByte(cHandle)
               );
         char[] sub = replacement().toCharArray();
         ec = NativeConverter.setCallbackDecode(cHandle,
                                                onMalformedInput,
                                                onUnmappableInput,
                                                sub, sub.length);
         if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
         }
         converterHandle=cHandle;
    }
    protected void implReplaceWith(String newReplacement) {
        if(converterHandle > 0){
            if( newReplacement.length() > NativeConverter.getMaxBytesPerChar(converterHandle)) {
                    throw new IllegalArgumentException();
            }           
            ec =NativeConverter.setSubstitutionChars(converterHandle,
                                                    newReplacement.toCharArray(),
                                                    newReplacement.length()
                                                    );
            if(ErrorCode.isFailure(ec)){
                throw ErrorCode.getException(ec);
            }
        }
     }
    protected final void implOnMalformedInput(CodingErrorAction newAction) {
        if(newAction.equals(CodingErrorAction.IGNORE)){
            onMalformedInput = NativeConverter.SKIP_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPLACE)){
            onMalformedInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPORT)){
            onMalformedInput = NativeConverter.STOP_CALLBACK;
        }
        char[] sub = replacement().toCharArray();
        ec = NativeConverter.setCallbackDecode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
        } 
    }
    protected final void implOnUnmappableCharacter(CodingErrorAction newAction) {
        if(newAction.equals(CodingErrorAction.IGNORE)){
            onUnmappableInput = NativeConverter.SKIP_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPLACE)){
            onUnmappableInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPORT)){
            onUnmappableInput = NativeConverter.STOP_CALLBACK;
        }
        char[] sub = replacement().toCharArray();
        ec = NativeConverter.setCallbackDecode(converterHandle,onMalformedInput, onUnmappableInput, sub, sub.length);
        if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
        } 
    }
    protected final CoderResult implFlush(CharBuffer out) {
       try{
           data[OUTPUT_OFFSET] = getArray(out);
            ec=NativeConverter.flushByteToChar(
                                            converterHandle,  
                                            output,           
                                            outEnd,           
                                            data              
                                            );
            if (ErrorCode.isFailure(ec)) {
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                }else if (ec == ErrorCode.U_TRUNCATED_CHAR_FOUND ) {
                    if(data[INPUT_OFFSET]>0){
                        return CoderResult.malformedForLength(data[INPUT_OFFSET]);
                    }
                }else {
                    ErrorCode.getException(ec);
                }
            }
            return CoderResult.UNDERFLOW;
       }finally{
            setPosition(out);
            implReset();
       }
    }
    protected void implReset() {
        NativeConverter.resetByteToChar(converterHandle);
        data[INPUT_OFFSET] = 0;
        data[OUTPUT_OFFSET] = 0;
        data[INVALID_BYTES] = 0;
        data[INPUT_HELD] = 0;
        savedInputHeldLen = 0;
        output = null;
        input = null;
    }
    protected CoderResult decodeLoop(ByteBuffer in,CharBuffer out){
        if(!in.hasRemaining()){
            return CoderResult.UNDERFLOW;
        }
        data[INPUT_OFFSET] = getArray(in);
        data[OUTPUT_OFFSET]= getArray(out);
        data[INPUT_HELD] = 0;
        try{
            ec=NativeConverter.decode(
                                converterHandle,  
                                input,            
                                inEnd,            
                                output,           
                                outEnd,           
                                data,             
                                false             
                                );
            if(ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR){
                return CoderResult.OVERFLOW;
            }else if(ec==ErrorCode.U_INVALID_CHAR_FOUND){
                return CoderResult.malformedForLength(data[INVALID_BYTES]);
            }else if(ec==ErrorCode.U_ILLEGAL_CHAR_FOUND){
                return CoderResult.malformedForLength(data[INVALID_BYTES]);
            }
            return CoderResult.UNDERFLOW;
        }finally{
            setPosition(in);
            setPosition(out);
        }
    }
    protected void finalize()throws Throwable{
        NativeConverter.closeConverter(converterHandle);
        super.finalize();
        converterHandle = 0;
    }
    private final int getArray(CharBuffer out){
        if(out.hasArray()){
            output = out.array();
            outEnd = out.arrayOffset() + out.limit();
            return out.arrayOffset() + out.position();
        }else{
            outEnd = out.remaining();
            if (allocatedOutput == null || (outEnd > allocatedOutput.length)) {
                allocatedOutput = new char[outEnd];
            }
            output = allocatedOutput;
            return 0;
        }
    }
    private  final int getArray(ByteBuffer in){
        if(in.hasArray()){
            input = in.array();
            inEnd = in.arrayOffset() + in.limit();
            return in.arrayOffset() + in.position() + savedInputHeldLen;
        }else{
            inEnd = in.remaining();
            if (allocatedInput == null || (inEnd > allocatedInput.length)) {
                allocatedInput = new byte[inEnd];
            }
            input = allocatedInput;
            int pos = in.position();
            in.get(input,0,inEnd);
            in.position(pos);
            return savedInputHeldLen;
        }
    }
    private final void setPosition(CharBuffer out){
        if(out.hasArray()){
            out.position(out.position() + data[OUTPUT_OFFSET] - out.arrayOffset());
        }else{
            out.put(output,0,data[OUTPUT_OFFSET]);
        }
        output = null;
    }
    private final void setPosition(ByteBuffer in){
        in.position(in.position() + data[INPUT_OFFSET] + savedInputHeldLen - data[INPUT_HELD]);
        savedInputHeldLen = data[INPUT_HELD];
        input = null;
    }
}
