public class CharToByteEUC_TW extends CharToByteConverter
{
    private final EUC_TW.Encoder enc = (EUC_TW.Encoder)(new EUC_TW().newEncoder());
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        reset();
        return 0;
    }
    public void reset() {
        byteOff = charOff = 0;
    }
    public boolean canConvert(char ch){
        return enc.canEncode(ch);
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        int outputSize;
        byte [] tmpbuf = new byte[4];;
        byte [] outputByte;
        byteOff = outOff;
        for (charOff = inOff; charOff < inEnd; charOff++) {
            outputByte = tmpbuf;
            if ( input[charOff] < 0x80) {       
                outputSize = 1;
                outputByte[0] = (byte)(input[charOff] & 0x7f);
            } else {
                outputSize = enc.toEUC(input[charOff], outputByte);
            }
            if (outputSize == -1) {
                if (subMode) {
                    outputByte = subBytes;
                    outputSize = subBytes.length;
                } else {
                    badInputLength = 1;
                    throw new UnknownCharacterException();
                }
            }
            if (outEnd - byteOff < outputSize)
                throw new ConversionBufferFullException();
            for (int i = 0; i < outputSize; i++)
                output[byteOff++] = outputByte[i];
        }
        return byteOff - outOff;
    }
    public int getMaxBytesPerChar() {
        return 4;
    }
    public String getCharacterEncoding() {
        return "EUC_TW";
    }
}
