public class ByteToCharISO8859_1 extends ByteToCharConverter {
    public String getCharacterEncoding()
    {
        return "ISO8859_1";
    }
    public int flush(char[] output, int outStart, int outEnd) {
        byteOff = charOff = 0;
        return 0;
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws ConversionBufferFullException
    {
        int bound = inOff + (outEnd - outOff);
        if (bound >= inEnd) {
             bound = inEnd;
        }
        int bytesWritten = inEnd - inOff;
        try {
            while(inOff < bound) {
                output[outOff++] = (char) (0xff & input[inOff++]);
            }
        } finally {
            charOff = outOff;
            byteOff = inOff;
        }
        if (bound < inEnd)
            throw new ConversionBufferFullException();
        return bytesWritten;
    }
    public void reset() {
        byteOff = charOff = 0;
    }
}
