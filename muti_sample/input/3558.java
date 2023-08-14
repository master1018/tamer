public abstract class ByteToCharSingleByte extends ByteToCharConverter {
    protected String byteToCharTable;
    public String getByteToCharTable() {
        return byteToCharTable;
    }
    public int flush(char[] output, int outStart, int outEnd) {
        byteOff = charOff = 0;
        return 0;
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException,
               MalformedInputException,
               ConversionBufferFullException
    {
        char    outputChar;
        int     byteIndex;
        charOff = outOff;
        byteOff = inOff;
        while(byteOff < inEnd) {
            byteIndex = input[byteOff];
            outputChar = getUnicode(byteIndex);
            if (outputChar == '\uFFFD') {
                if (subMode) {
                    outputChar = subChars[0];
                } else {
                    badInputLength = 1;
                    throw new UnknownCharacterException();
                }
            }
            if (charOff >= outEnd)
                throw new ConversionBufferFullException();
            output[charOff]= outputChar;
            charOff++;
            byteOff++;
        }
        return charOff-outOff;
    }
    protected char getUnicode(int byteIndex) {
        int n = byteIndex + 128;
        if (n >= byteToCharTable.length() || n < 0)
            return '\uFFFD';
        return byteToCharTable.charAt(n);
    }
    public void reset() {
        byteOff = charOff = 0;
    }
}
