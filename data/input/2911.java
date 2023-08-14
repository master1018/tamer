public abstract class ByteToCharEUC extends ByteToCharConverter
{
    private final int G0 = 0;
    private final int G1 = 1;
    private final int SS2 =  0x8E;
    private final int SS3 =  0x8F;
    private int firstByte, state;
    protected String  mappingTableG1;
    protected String  byteToCharTable;
    public ByteToCharEUC() {
        super();
        state = G0;
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
       char      outputChar = '\uFFFD';
       byteOff = inOff;
       charOff = outOff;
       while (byteOff < inEnd) {
          byte1 = input[byteOff];
          if (byte1 < 0)
             byte1 += 256;
          switch (state) {
             case G0:
                if (byte1 == SS2 ||                
                    byte1 == SS3 ) {               
                   badInputLength = 1;
                   throw new MalformedInputException();
                }
                if ( byte1 <= 0x9f )               
                   outputChar = byteToCharTable.charAt(byte1);
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
                outputChar = mappingTableG1.charAt(((firstByte - 0xa1) * 94) + byte1 - 0xa1);
                break;
          }
          if (state == G0) {
             if (outputChar == '\uFFFD') {
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
