public abstract class CharToByteEUC extends CharToByteConverter
{
    private char highHalfZoneCode;
    private byte[] outputByte;
    protected short  index1[];
    protected String index2;
    protected String index2a;
    protected String index2b;
    protected String index2c;
    protected int    mask1;
    protected int    mask2;
    protected int    shift;
    private byte[] workByte = new byte[4];
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
           outputByte = workByte;
           int     index;
           int     theBytes;
           int     spaceNeeded;
           boolean allZeroes = true;
           int     i;
           if (highHalfZoneCode == 0) {
              inputChar = input[charOff];
              inputSize = 1;
           } else {
              inputChar = highHalfZoneCode;
              inputSize = 0;
              highHalfZoneCode = 0;
           }
           if(inputChar >= '\ud800' && inputChar <= '\udbff') {
              if (charOff + inputSize >= inEnd) {
                 highHalfZoneCode = inputChar;
                 charOff += inputSize;
                 break;
              }
              inputChar = input[charOff + inputSize];
              if (inputChar >= '\udc00' && inputChar <= '\udfff') {
                 if (subMode) {
                    outputByte = subBytes;
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
           else
              if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                 badInputLength = 1;
                 throw new MalformedInputException();
              } else {
                 String theChars;
                 char   aChar;
                 index = index1[((inputChar & mask1) >> shift)] + (inputChar & mask2);
                 if (index < 7500)
                   theChars = index2;
                 else
                   if (index < 15000) {
                     index = index - 7500;
                     theChars = index2a;
                   }
                   else
                     if (index < 22500){
                       index = index - 15000;
                       theChars = index2b;
                     }
                     else {
                       index = index - 22500;
                       theChars = index2c;
                     }
                 aChar = theChars.charAt(2*index);
                 outputByte[0] = (byte)((aChar & 0xff00)>>8);
                 outputByte[1] = (byte)(aChar & 0x00ff);
                 aChar = theChars.charAt(2*index + 1);
                 outputByte[2] = (byte)((aChar & 0xff00)>>8);
                 outputByte[3] = (byte)(aChar & 0x00ff);
              }
           for (i = 0; i < outputByte.length; i++) {
             if (outputByte[i] != 0x00) {
               allZeroes = false;
               break;
             }
           }
           if (allZeroes && inputChar != '\u0000')
           {
              if (subMode) {
                 outputByte = subBytes;
              } else {
                badInputLength = 1;
                throw new UnknownCharacterException();
              }
           }
           int oindex = 0;
           for (spaceNeeded = outputByte.length; spaceNeeded > 1; spaceNeeded--){
             if (outputByte[oindex++] != 0x00 )
               break;
           }
           if (byteOff + spaceNeeded > outEnd)
              throw new ConversionBufferFullException();
           for (i = outputByte.length - spaceNeeded; i < outputByte.length; i++) {
              output[byteOff++] = outputByte[i];
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
    public boolean canConvert(char ch) {
       int    index;
       String theChars;
       index = index1[((ch & mask1) >> shift)] + (ch & mask2);
       if (index < 7500)
         theChars = index2;
       else
         if (index < 15000) {
           index = index - 7500;
           theChars = index2a;
         }
         else
           if (index < 22500){
             index = index - 15000;
             theChars = index2b;
           }
           else {
             index = index - 22500;
             theChars = index2c;
           }
       if (theChars.charAt(2*index) != '\u0000' ||
                    theChars.charAt(2*index + 1) != '\u0000')
         return (true);
       return( ch == '\u0000');
    }
}
