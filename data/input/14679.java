public abstract class ByteToCharEUC2 extends ByteToCharConverter
{
    private final int G0 = 0;
    private final int G1 = 1;
    private final int SS2 =  0x8E;
    private final int SS3 =  0x8F;
    private int firstByte, state;
    private DoubleByte.Decoder dec;
    public ByteToCharEUC2(DoubleByte.Decoder dec) {
        super();
        state = G0;
        this.dec = dec;
    }
    char decodeSingle(int b) {
        return dec.decodeSingle(b);
    }
    char decodeDouble(int b1, int b2) {
        return dec.decodeDouble(b1, b2);
    }
    public int flush(char[] output, int outStart, int outEnd)
       throws MalformedInputException
    {
       if (state != G0) {
          reset();
          badInputLength = 0;
          throw new MalformedInputException();
       }
       reset();
       return 0;
    }
    public void reset() {
       state = G0;
       charOff = byteOff = 0;
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        int       byte1;
        char      outputChar = UNMAPPABLE_DECODING;
        byteOff = inOff;
        charOff = outOff;
        while (byteOff < inEnd) {
            byte1 = input[byteOff] & 0xff;
            switch (state) {
            case G0:
                if (byte1 == SS2 ||                
                    byte1 == SS3 ) {               
                    badInputLength = 1;
                    throw new MalformedInputException();
                }
                if ( byte1 <= 0x9f )               
                    outputChar = decodeSingle(byte1);
                else
                    if (byte1 < 0xa1 || byte1 > 0xfe) {  
                        badInputLength = 1;
                        throw new MalformedInputException();
                    } else {                       
                        firstByte = byte1;
                        state = G1;
                    }
                break;
            case G1:
                state = G0;
                if ( byte1 < 0xa1 || byte1 > 0xfe) {  
                    badInputLength = 1;
                    throw new MalformedInputException();
                }
                outputChar = decodeDouble(firstByte, byte1);
                break;
            }
            if (state == G0) {
                if (outputChar == UNMAPPABLE_DECODING) {
                    if (subMode)
                        outputChar = subChars[0];
                    else {
                        badInputLength = 1;
                        throw new UnknownCharacterException();
                    }
                }
                if (charOff >= outEnd)
                    throw new ConversionBufferFullException();
                output[charOff++] = outputChar;
            }
            byteOff++;
         }
         return charOff - outOff;
    }
}
