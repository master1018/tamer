public class ByteToCharEUC_JP extends ByteToCharJIS0208 {
    private byte savedSecond = 0;
    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();
    ByteToCharJIS0212 bcJIS0212 = new ByteToCharJIS0212();
    public ByteToCharEUC_JP() {
        super();
        start = 0xA1;
        end = 0xFE;
        savedSecond = 0;
    }
    public int flush(char[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (savedSecond != 0) {
            reset();
            throw new MalformedInputException();
        }
        reset();
        return 0;
    }
    public void reset() {
        super.reset();
        savedSecond = 0;
    }
    public String getCharacterEncoding() {
        return "EUC_JP";
    }
    protected char convSingleByte(int b) {
        if (b < 0 || b > 0x7F)
            return REPLACE_CHAR;
        return bcJIS0201.getUnicode(b);
    }
    protected char getUnicode(int byte1, int byte2) {
        if (byte1 == 0x8E) {
            return bcJIS0201.getUnicode(byte2 - 256);
        }
        if (((byte1 < 0) || (byte1 > index1.length))
            || ((byte2 < start) || (byte2 > end)))
            return REPLACE_CHAR;
        int n = (index1[byte1 - 0x80] & 0xf) * (end - start + 1)
                + (byte2 - start);
        return index2[index1[byte1 - 0x80] >> 4].charAt(n);
    }
    protected char decode0212(int byte1, int byte2) {
        return bcJIS0212.getUnicode(byte1, byte2);
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException,
               ConversionBufferFullException
    {
        char    outputChar = REPLACE_CHAR;
        int     inputSize = 0;          
        charOff = outOff;
        byteOff = inOff;
        while (byteOff < inEnd) {
            int byte1, byte2;
            if (savedByte == 0) {
                byte1 = input[byteOff];
                inputSize = 1;
            } else {
                byte1 = savedByte;
                savedByte = 0;
                inputSize = 0;
            }
            outputChar = convSingleByte(byte1);
            if (outputChar == REPLACE_CHAR) {   
                if ((byte1 & 0xff) == 0x8F) {   
                    if (byteOff + inputSize + 1 >= inEnd) {
                        savedByte = (byte) byte1;
                        byteOff += inputSize;
                        if (byteOff < inEnd) {
                            savedSecond = input[byteOff];
                            byteOff++;
                        }
                        break;
                    }
                    if (savedSecond != 0) {
                        byte1 = savedSecond & 0xff;
                        savedSecond = 0;
                    } else {
                        byte1 = input[byteOff + inputSize] & 0xff;
                        inputSize++;
                    }
                    byte2 = input[byteOff + inputSize] & 0xff;
                    inputSize++;
                    outputChar = decode0212(byte1-0x80, byte2-0x80);
                } else { 
                    if (byteOff + inputSize >= inEnd) {
                        savedByte = (byte) byte1;
                        byteOff += inputSize;
                        break;
                    }
                    byte1 &= 0xff;
                    byte2 = input[byteOff + inputSize] & 0xff;
                    inputSize++;
                    outputChar = getUnicode(byte1, byte2);
                }
            }
            if (outputChar == REPLACE_CHAR) {
                if (subMode)
                    outputChar = subChars[0];
                else {
                    badInputLength = inputSize;
                    throw new UnknownCharacterException();
                }
            }
            if (charOff >= outEnd)
                throw new ConversionBufferFullException();
            output[charOff++] = outputChar;
            byteOff += inputSize;
        }
        return charOff - outOff;
    }
}
