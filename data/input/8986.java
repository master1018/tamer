public abstract class CharToByteDBCS_ASCII extends CharToByteConverter
{
    private char highHalfZoneCode;
    private byte[] outputByte = new byte[2];
    private DoubleByte.Encoder enc;
    public CharToByteDBCS_ASCII(DoubleByte.Encoder enc) {
        super();
        this.enc = enc;
    }
    int encodeChar(char c) {
        return enc.encodeChar(c);
    }
    public int flush(byte [] output, int outStart, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {
       if (highHalfZoneCode != 0) {
          reset();
          badInputLength = 0;
          throw new MalformedInputException();
       }
       reset();
       return 0;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        char    inputChar;
        int     inputSize;
        byteOff = outOff;
        charOff = inOff;
        while(charOff < inEnd) {
            int   index;
            int   theBytes;
            int   spaceNeeded;
            if (highHalfZoneCode == 0) {
                inputChar = input[charOff];
                inputSize = 1;
            } else {
                inputChar = highHalfZoneCode;
                inputSize = 0;
                highHalfZoneCode = 0;
            }
            if (Character.isHighSurrogate(inputChar)) {
                if (charOff + inputSize >= inEnd) {
                    highHalfZoneCode = inputChar;
                    charOff += inputSize;
                    break;
                }
                inputChar = input[charOff + inputSize];
                if (Character.isLowSurrogate(inputChar)) {
                    if (subMode) {
                        if (subBytes.length == 1) {
                            outputByte[0] = 0x00;
                            outputByte[1] = subBytes[0];
                        }
                        else {
                            outputByte[0] = subBytes[0];
                            outputByte[1] = subBytes[1];
                        }
                        inputSize++;
                    } else {
                        badInputLength = 2;
                        throw new UnknownCharacterException();
                    }
                 } else {
                     badInputLength = 1;
                     throw new MalformedInputException();
                 }
            }
            else if (Character.isLowSurrogate(inputChar)) {
                badInputLength = 1;
                throw new MalformedInputException();
            } else {
                theBytes = encodeChar(inputChar);
                if (theBytes == UNMAPPABLE_ENCODING) {
                    if (subMode) {
                        if (subBytes.length == 1) {
                            outputByte[0] = 0x00;
                            outputByte[1] = subBytes[0];
                        } else {
                            outputByte[0] = subBytes[0];
                            outputByte[1] = subBytes[1];
                        }
                    } else {
                        badInputLength = 1;
                        throw new UnknownCharacterException();
                    }
                } else {
                    outputByte[0] = (byte)(theBytes >>8);
                    outputByte[1] = (byte)theBytes;
                }
            }
            if (outputByte[0] == 0x00)
                spaceNeeded = 1;
            else
                spaceNeeded = 2;
            if (byteOff + spaceNeeded > outEnd)
                throw new ConversionBufferFullException();
            if (spaceNeeded == 1)
                output[byteOff++] = outputByte[1];
            else {
                output[byteOff++] = outputByte[0];
                output[byteOff++] = outputByte[1];
            }
            charOff += inputSize;
        }
        return byteOff - outOff;
    }
    public void reset() {
       charOff = byteOff = 0;
       highHalfZoneCode = 0;
    }
    public int getMaxBytesPerChar() {
        return 2;
    }
    public boolean canConvert(char c) {
        return encodeChar(c) != UNMAPPABLE_ENCODING;
    }
}
