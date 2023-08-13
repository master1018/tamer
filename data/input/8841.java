public class CharToByteASCII extends CharToByteConverter {
    public String getCharacterEncoding()
    {
        return "ASCII";
    }
    private char highHalfZoneCode;
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (highHalfZoneCode != 0) {
            highHalfZoneCode = 0;
            throw new MalformedInputException
                ("String ends with <High Half Zone code> of UTF16");
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
        byte[]  tmpArray = new byte[1];
        int     inputSize;          
        int     outputSize;         
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
                throw new MalformedInputException
                    ("Previous converted string ends with " +
                     "<High Half Zone Code> of UTF16 " +
                     ", but this string is not begin with <Low Half Zone>");
            }
        }
        while(charOff < inEnd) {
            outputByte = tmpArray;
            inputChar = input[charOff];
            outputSize = 1;
            inputSize = 1;
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                if (charOff + 1 == inEnd) {
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
                if (inputChar <= '\u007F') {
                    outputByte[0] = (byte)inputChar;
                } else {
                    if (subMode) {
                        outputByte = subBytes;
                        outputSize = subBytes.length;
                    } else {
                        badInputLength = 1;
                        throw new UnknownCharacterException();
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
        return byteOff-outOff;
    }
    public boolean canConvert(char ch)
    {
        return (ch <= '\u007F');
    }
    public void reset()
    {
        byteOff = charOff = 0;
        highHalfZoneCode = 0;
    }
    public int getMaxBytesPerChar()
    {
        return 1;
    }
}
