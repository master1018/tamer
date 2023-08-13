public class ByteToCharISCII91 extends ByteToCharConverter {
    private static final char[] directMapTable = ISCII91.getDirectMapTable();
    private static final char NUKTA_CHAR = '\u093c';
    private static final char HALANT_CHAR = '\u094d';
    private static final char ZWNJ_CHAR = '\u200c';
    private static final char ZWJ_CHAR = '\u200d';
    private static final char INVALID_CHAR = '\uffff';
    private char contextChar = INVALID_CHAR;
    private boolean needFlushing = false;
    public int convert(byte input[], int inStart, int inEnd,
                        char output[], int outStart, int outEnd)
    throws ConversionBufferFullException, UnknownCharacterException {
        charOff = outStart;
        byteOff = inStart;
        while (byteOff < inEnd) {
            if (charOff >= outEnd) {
                throw new ConversionBufferFullException();
            }
            int index = input[byteOff++];
            index = ( index < 0 )? ( index + 255 ):index;
            char currentChar = directMapTable[index];
            if(contextChar == '\ufffd') {
                output[charOff++] = '\ufffd';
                contextChar = INVALID_CHAR;
                needFlushing = false;
                continue;
            }
            switch(currentChar) {
            case '\u0901':
            case '\u0907':
            case '\u0908':
            case '\u090b':
            case '\u093f':
            case '\u0940':
            case '\u0943':
            case '\u0964':
                if(needFlushing) {
                    output[charOff++] = contextChar;
                    contextChar = currentChar;
                    continue;
                }
                contextChar = currentChar;
                needFlushing = true;
                continue;
            case NUKTA_CHAR:
                switch(contextChar) {
                case '\u0901':
                    output[charOff] = '\u0950';
                    break;
                case '\u0907':
                    output[charOff] = '\u090c';
                    break;
                case '\u0908':
                    output[charOff] = '\u0961';
                    break;
                case '\u090b':
                    output[charOff] = '\u0960';
                    break;
                case '\u093f':
                    output[charOff] = '\u0962';
                    break;
                case '\u0940':
                    output[charOff] = '\u0963';
                    break;
                case '\u0943':
                    output[charOff] = '\u0944';
                    break;
                case '\u0964':
                    output[charOff] = '\u093d';
                    break;
                case HALANT_CHAR:
                    if(needFlushing) {
                        output[charOff++] = contextChar;
                        contextChar = currentChar;
                        continue;
                    }
                    output[charOff] = ZWJ_CHAR;
                    break;
                default:
                    if(needFlushing) {
                        output[charOff++] = contextChar;
                        contextChar = currentChar;
                        continue;
                    }
                    output[charOff] = NUKTA_CHAR;
                }
                break;
            case HALANT_CHAR:
                if(needFlushing) {
                    output[charOff++] = contextChar;
                    contextChar = currentChar;
                    continue;
                }
                if(contextChar == HALANT_CHAR) {
                    output[charOff] = ZWNJ_CHAR;
                    break;
                }
                output[charOff] = HALANT_CHAR;
                break;
            case INVALID_CHAR:
                if(needFlushing) {
                    output[charOff++] = contextChar;
                    contextChar = currentChar;
                    continue;
                }
                if(subMode) {
                    output[charOff] = subChars[0];
                    break;
                } else {
                    contextChar = INVALID_CHAR;
                    throw new UnknownCharacterException();
                }
            default:
                if(needFlushing) {
                    output[charOff++] = contextChar;
                    contextChar = currentChar;
                    continue;
                }
                output[charOff] = currentChar;
                break;
        }
        contextChar = currentChar;
        needFlushing = false;
        charOff++;
        }
        return charOff - outStart;
    } 
    public  int flush( char[] output, int outStart, int outEnd )
    throws MalformedInputException, ConversionBufferFullException
    {
        int charsWritten = 0;
        if(needFlushing) {
            output[outStart] = contextChar;
            charsWritten = 1;
        }
        contextChar = INVALID_CHAR;
        needFlushing = false;
        byteOff = charOff = 0;
        return charsWritten;
    }
    public String getCharacterEncoding()
    {
        return "ISCII91";
    }
    public void reset()
    {
        byteOff = charOff = 0;
    }
}
