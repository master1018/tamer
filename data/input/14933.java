public abstract class CharToByteConverter {
    protected boolean subMode = true;
    protected byte[] subBytes = { (byte)'?' };
    protected int charOff;
    protected int byteOff;
    protected int badInputLength;
    public static CharToByteConverter getDefault() {
        Object cvt;
        cvt = Converters.newDefaultConverter(Converters.CHAR_TO_BYTE);
        return (CharToByteConverter)cvt;
    }
    public static CharToByteConverter getConverter(String encoding)
        throws UnsupportedEncodingException
    {
        Object cvt;
        cvt = Converters.newConverter(Converters.CHAR_TO_BYTE, encoding);
        return (CharToByteConverter)cvt;
    }
    public abstract String getCharacterEncoding();
    public abstract int convert(char[] input, int inStart, int inEnd,
                                byte[] output, int outStart, int outEnd)
        throws MalformedInputException,
               UnknownCharacterException,
               ConversionBufferFullException;
    public int convertAny(char[] input, int inStart, int inEnd,
                          byte[] output, int outStart, int outEnd)
        throws ConversionBufferFullException
    {
        if (!subMode) {             
            throw new IllegalStateException("Substitution mode is not on");
        }
        int localInOff = inStart;
        int localOutOff = outStart;
        while(localInOff < inEnd) {
            try {
                int discard = convert(input, localInOff, inEnd,
                                      output, localOutOff, outEnd);
                return (nextByteIndex() - outStart);
            } catch (MalformedInputException e) {
                byte[] s = subBytes;
                int subSize = s.length;
                localOutOff = nextByteIndex();
                if ((localOutOff + subSize) > outEnd)
                    throw new ConversionBufferFullException();
                for (int i = 0; i < subSize; i++)
                    output[localOutOff++] = s[i];
                localInOff = nextCharIndex();
                localInOff += badInputLength;
                badInputLength = 0;
                if (localInOff >= inEnd){
                    byteOff = localOutOff;
                    return (byteOff - outStart);
                }
                continue;
            }catch (UnknownCharacterException e) {
                throw new Error("UnknownCharacterException thrown "
                                + "in substititution mode",
                                e);
            }
        }
        return (nextByteIndex() - outStart);
    }
    public byte[] convertAll( char input[] ) throws MalformedInputException {
        reset();
        boolean savedSubMode = subMode;
        subMode = true;
        byte[] output = new byte[ getMaxBytesPerChar() * input.length ];
        try {
            int outputLength = convert( input, 0, input.length,
                                        output, 0, output.length );
            outputLength += flush( output, nextByteIndex(), output.length );
            byte [] returnedOutput = new byte[ outputLength ];
            System.arraycopy( output, 0, returnedOutput, 0, outputLength );
            return returnedOutput;
        }
        catch( ConversionBufferFullException e ) {
            throw new
                InternalError("this.getMaxBytesPerChar returned bad value");
        }
        catch( UnknownCharacterException e ) {
            throw new InternalError();
        }
        finally {
            subMode = savedSubMode;
        }
    }
    public abstract int flush( byte[] output, int outStart, int outEnd )
        throws MalformedInputException, ConversionBufferFullException;
    public int flushAny( byte[] output, int outStart, int outEnd )
        throws ConversionBufferFullException
    {
        if (!subMode) {             
            throw new IllegalStateException("Substitution mode is not on");
        }
        try {
            return flush(output, outStart, outEnd);
        } catch (MalformedInputException e) {
            int subSize = subBytes.length;
            byte[] s = subBytes;
            int outIndex = outStart;
            if ((outStart + subSize) > outEnd)
                throw new ConversionBufferFullException();
            for (int i = 0; i < subSize; i++)
                output[outIndex++] = s[i];
            byteOff = charOff = 0; 
            badInputLength = 0;
            return subSize;
        }
    }
    public abstract void reset();
    public boolean canConvert(char c) {
        try {
            char[] input = new char[1];
            byte[] output = new byte[3];
            input[0] = c;
            convert(input, 0, 1, output, 0, 3);
            return true;
        } catch(CharConversionException e){
            return false;
        }
    }
    public abstract int getMaxBytesPerChar();
    public int getBadInputLength() {
        return badInputLength;
    }
    public int nextCharIndex() {
        return charOff;
    }
    public int nextByteIndex() {
        return byteOff;
    }
    public void setSubstitutionMode(boolean doSub) {
        subMode = doSub;
    }
    public void setSubstitutionBytes( byte[] newSubBytes )
        throws IllegalArgumentException
    {
        if( newSubBytes.length > getMaxBytesPerChar() ) {
            throw new IllegalArgumentException();
        }
        subBytes = new byte[ newSubBytes.length ];
        System.arraycopy( newSubBytes, 0, subBytes, 0, newSubBytes.length );
    }
    public String toString() {
        return "CharToByteConverter: " + getCharacterEncoding();
    }
}
