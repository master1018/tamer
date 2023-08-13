public abstract class ByteToCharDBCS_EBCDIC extends ByteToCharConverter
{
    private static final int SBCS = 0;
    private static final int DBCS = 1;
    private static final int SO = 0x0e;
    private static final int SI = 0x0f;
    private int  currentState;
    private boolean savedBytePresent;
    private int savedByte;
    private DoubleByte.Decoder dec;
    public ByteToCharDBCS_EBCDIC(DoubleByte.Decoder dec) {
       super();
       currentState = SBCS;
       savedBytePresent = false;
       this.dec = dec;
    }
    char decodeSingle(int b) {
        return dec.decodeSingle(b);
    }
    char decodeDouble(int b1, int b2) {
        return dec.decodeDouble(b1, b2);
    }
    public int flush(char [] output, int outStart, int outEnd)
       throws MalformedInputException
    {
       if (savedBytePresent) {
           reset();
           badInputLength = 0;
           throw new MalformedInputException();
       }
       reset();
       return 0;
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
       int  inputSize;
       char outputChar = UNMAPPABLE_DECODING;
       charOff = outOff;
       byteOff = inOff;
       while(byteOff < inEnd) {
          int byte1, byte2;
          if (!savedBytePresent) {
            byte1 = input[byteOff] & 0xff;
            inputSize = 1;
          } else {
            byte1 = savedByte;
            savedBytePresent = false;
            inputSize = 0;
          }
          if (byte1 == SO) {
             if (currentState != SBCS) {
                badInputLength = 1;
                throw new MalformedInputException();
             } else {
                currentState = DBCS;
                byteOff += inputSize;
             }
          }
          else
             if (byte1 == SI) {
                if (currentState != DBCS) {
                   badInputLength = 1;
                   throw new MalformedInputException();
                } else {
                   currentState = SBCS;
                   byteOff+= inputSize;
                }
             } else {
                if (currentState == SBCS) {
                   outputChar = decodeSingle(byte1);
                } else {
                   if (byte1 < 0x40 || byte1 > 0xfe) {
                      badInputLength = 1;
                      throw new MalformedInputException();
                   }
                   if (byteOff + inputSize >= inEnd) {
                      savedByte = byte1;
                      savedBytePresent = true;
                      byteOff += inputSize;
                      break;
                   }
                   byte2 = input[byteOff+inputSize] & 0xff;
                   inputSize++;
                   if ((byte1 != 0x40 || byte2 != 0x40) &&
                      (byte2 < 0x41 || byte2 > 0xfe)) {
                      badInputLength = 2;
                      throw new MalformedInputException();
                   }
                   outputChar = decodeDouble(byte1, byte2);
                }
                if (outputChar == UNMAPPABLE_DECODING) {
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
       }
       return charOff - outOff;
    }
    public void reset() {
       charOff = byteOff = 0;
       currentState = SBCS;
       savedBytePresent = false;
    }
}
