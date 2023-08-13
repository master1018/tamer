public abstract class CharToByteSingleByte extends CharToByteConverter {
    protected char[] index1;
    protected char[] index2;
    protected int   mask1;
    protected int   mask2;
    protected int   shift;
    private char highHalfZoneCode;
    public char[] getIndex1() {
        return index1;
    }
    public char[] getIndex2() {
        return index2;
    }
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (highHalfZoneCode != 0) {
            highHalfZoneCode = 0;
            badInputLength = 0;
            throw new MalformedInputException();
        }
        byteOff = charOff = 0;
        return 0;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws MalformedInputException,
               UnknownCharacterException,
               ConversionBufferFullException
    {
        char    inputChar;          
        byte[]  outputByte;         
        int     inputSize;          
        int     outputSize;         
        byte[]  tmpArray = new byte[1];
        charOff = inOff;
        byteOff = outOff;
        if (highHalfZoneCode != 0) {
            inputChar = highHalfZoneCode;
            highHalfZoneCode = 0;
            if (input[inOff] >= 0xdc00 && input[inOff] <= 0xdfff) {
                badInputLength = 1;
                throw new UnknownCharacterException();
            } else {
                badInputLength = 0;
                throw new MalformedInputException();
            }
        }
        while(charOff < inEnd) {
            outputByte = tmpArray;
            inputChar = input[charOff];
            outputSize = 1;
            inputSize = 1;
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                if (charOff + 1 >= inEnd) {
                    highHalfZoneCode = inputChar;
                    break;
                }
                inputChar = input[charOff + 1];
                if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                    if (subMode) {
                        outputByte = subBytes;
                        outputSize = subBytes.length;
                        inputSize = 2;
                    } else {
                        badInputLength = 2;
                        throw new UnknownCharacterException();
                    }
                } else {
                    badInputLength = 1;
                    throw new MalformedInputException();
                }
            }
            else if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                badInputLength = 1;
                throw new MalformedInputException();
            }
            else {
                outputByte[0] = getNative(inputChar);
                if (outputByte[0] == 0) {
                    if (input[charOff] != '\u0000') {
                        if (subMode) {
                            outputByte = subBytes;
                            outputSize = subBytes.length;
                        } else {
                            badInputLength = 1;
                            throw new UnknownCharacterException();
                        }
                    }
                }
            }
            if (byteOff + outputSize > outEnd)
                throw new ConversionBufferFullException();
            for (int i = 0; i < outputSize; i++) {
                output[byteOff++] = outputByte[i];
            }
            charOff += inputSize;
        }
        return byteOff - outOff;
    }
    public int getMaxBytesPerChar() {
        return 1;
    }
    int encodeChar(char ch) {
        char index = index1[ch >> 8];
        if (index == UNMAPPABLE_ENCODING)
            return UNMAPPABLE_ENCODING;
        return index2[index + (ch & 0xff)];
    }
    public byte getNative(char inputChar) {
        int b = encodeChar(inputChar);
        if (b == UNMAPPABLE_ENCODING)
            return 0;
        return (byte)b;
    }
    public void reset() {
        byteOff = charOff = 0;
        highHalfZoneCode = 0;
    }
    public boolean canConvert(char ch) {
        return encodeChar(ch) != UNMAPPABLE_ENCODING;
    }
}
