public class CharToByteISO2022JP extends CharToByteJIS0208 {
    private static final int ASCII = 0;                 
    private static final int JISX0201_1976 = 1;         
    private static final int JISX0208_1978 = 2;         
    private static final int JISX0208_1983 = 3;         
    private static final int JISX0201_1976_KANA = 4;    
    private char highHalfZoneCode;
    private boolean flushed = true;
    private int currentMode = ASCII;
    protected byte[] subBytesEscape = { (byte)0x1b, (byte)0x28, (byte)0x42 }; 
    protected int subBytesMode = ASCII;
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {
        if (highHalfZoneCode != 0) {
            highHalfZoneCode = 0;
            badInputLength = 0;
            throw new MalformedInputException();
        }
        if (!flushed && (currentMode != ASCII)) {
            if (outEnd - outStart < 3) {
                throw new ConversionBufferFullException();
            }
            output[outStart]     = (byte)0x1b;
            output[outStart + 1] = (byte)0x28;
            output[outStart + 2] = (byte)0x42;
            byteOff += 3;
            byteOff = charOff = 0;
            flushed = true;
            currentMode = ASCII;
            return 3;
        }
        return 0;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws MalformedInputException, UnknownCharacterException,
               ConversionBufferFullException
    {
        char    inputChar;          
        int     inputSize;          
        int     outputSize;         
        byte[]  tmpArray = new byte[6];
        byte[]  outputByte;
        flushed = false;
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
            int newMode = currentMode; 
            inputChar = input[charOff];
            inputSize = 1;
            outputSize = 1;
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                if (charOff + 1 >= inEnd) {
                    highHalfZoneCode = inputChar;
                    break;
                }
                inputChar = input[charOff + 1];
                if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                    if (subMode) {
                        if (currentMode != subBytesMode) {
                            System.arraycopy(subBytesEscape, 0, outputByte, 0,
                                             subBytesEscape.length);
                            outputSize = subBytesEscape.length;
                            System.arraycopy(subBytes, 0, outputByte,
                                             outputSize, subBytes.length);
                            outputSize += subBytes.length;
                            newMode = subBytesMode;
                        } else {
                            outputByte = subBytes;
                            outputSize = subBytes.length;
                        }
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
            } else {
                if (inputChar <= '\u007F') {
                    if (currentMode != ASCII) {
                        outputByte[0] = (byte)0x1b;
                        outputByte[1] = (byte)0x28;
                        outputByte[2] = (byte)0x42;
                        outputByte[3] = (byte)inputChar;
                        outputSize = 4;
                        newMode = ASCII;
                    } else {
                        outputByte[0] = (byte)inputChar;
                        outputSize = 1;
                    }
                }
                else if (inputChar >= 0xFF61 && inputChar <= 0xFF9F) {
                    if (currentMode != JISX0201_1976_KANA) {
                        outputByte[0] = (byte)0x1b;
                        outputByte[1] = (byte)0x28;
                        outputByte[2] = (byte)0x49;
                        outputByte[3] = (byte)(inputChar - 0xff40);
                        outputSize = 4;
                        newMode = JISX0201_1976_KANA;
                    } else {
                        outputByte[0] = (byte)(inputChar - 0xff40);
                        outputSize = 1;
                    }
                }
                else if (inputChar == '\u00A5') {
                    if (currentMode != JISX0201_1976) {
                        outputByte[0] = (byte)0x1b;
                        outputByte[1] = (byte)0x28;
                        outputByte[2] = (byte)0x4a;
                        outputByte[3] = (byte)0x5c;
                        outputSize = 4;
                        newMode = JISX0201_1976;
                    } else {
                        outputByte[0] = (byte)0x5C;
                        outputSize = 1;
                    }
                }
                else if (inputChar == '\u203E')
                    {
                        if (currentMode != JISX0201_1976) {
                            outputByte[0] = (byte)0x1b;
                            outputByte[1] = (byte)0x28;
                            outputByte[2] = (byte)0x4a;
                            outputByte[3] = (byte)0x7e;
                            outputSize = 4;
                            newMode = JISX0201_1976;
                        } else {
                            outputByte[0] = (byte)0x7e;
                            outputSize = 1;
                        }
                    }
                else {
                    int index = getNative(inputChar);
                    if (index != 0) {
                        if (currentMode != JISX0208_1983) {
                            outputByte[0] = (byte)0x1b;
                            outputByte[1] = (byte)0x24;
                            outputByte[2] = (byte)0x42;
                            outputByte[3] = (byte)(index >> 8);
                            outputByte[4] = (byte)(index & 0xff);
                            outputSize = 5;
                            newMode = JISX0208_1983;
                        } else {
                            outputByte[0] = (byte)(index >> 8);
                            outputByte[1] = (byte)(index & 0xff);
                            outputSize = 2;
                        }
                    }
                    else {
                        if (subMode) {
                            if (currentMode != subBytesMode) {
                                System.arraycopy(subBytesEscape, 0, outputByte, 0,
                                                 subBytesEscape.length);
                                outputSize = subBytesEscape.length;
                                System.arraycopy(subBytes, 0, outputByte,
                                                 outputSize, subBytes.length);
                                outputSize += subBytes.length;
                                newMode = subBytesMode;
                            } else {
                                outputByte = subBytes;
                                outputSize = subBytes.length;
                            }
                        } else {
                            badInputLength = 1;
                            throw new UnknownCharacterException();
                        }
                    }
                }
            }
            if (byteOff + outputSize > outEnd)
                throw new ConversionBufferFullException();
            for ( int i = 0 ; i < outputSize ; i++ )
                output[byteOff++] = outputByte[i];
            charOff += inputSize;
            currentMode = newMode;
        }
        if (currentMode != ASCII){
            if (byteOff + 3 > outEnd)
                throw new ConversionBufferFullException();
            output[byteOff++] = 0x1b;
            output[byteOff++] = 0x28;
            output[byteOff++] = 0x42;
            currentMode = ASCII;
        }
        return byteOff-outOff;
    }
    public void reset() {
        highHalfZoneCode = 0;
        byteOff = charOff = 0;
        currentMode = ASCII;
    }
    public int getMaxBytesPerChar() {
        return 8;
    }
    public String getCharacterEncoding() {
        return "ISO2022JP";
    }
}
