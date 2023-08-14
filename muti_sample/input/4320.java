public abstract class ByteToCharConverter {
    protected boolean subMode = true;
    protected char[] subChars = { '\uFFFD' };
    protected int charOff;
    protected int byteOff;
    protected int badInputLength;
    public static ByteToCharConverter getDefault() {
        Object cvt;
        cvt = Converters.newDefaultConverter(Converters.BYTE_TO_CHAR);
        return (ByteToCharConverter)cvt;
    }
    public static ByteToCharConverter getConverter(String encoding)
        throws UnsupportedEncodingException
    {
        Object cvt;
        cvt = Converters.newConverter(Converters.BYTE_TO_CHAR, encoding);
        return (ByteToCharConverter)cvt;
    }
    public abstract String getCharacterEncoding();
    public abstract int convert(byte[] input, int inStart, int inEnd,
                                char[] output, int outStart, int outEnd)
            throws MalformedInputException,
                   UnknownCharacterException,
                   ConversionBufferFullException;
    public char[] convertAll( byte input[] ) throws MalformedInputException {
        reset();
        boolean savedSubMode = subMode;
        subMode = true;
        char[] output = new char[ getMaxCharsPerByte() * input.length ];
        try {
            int outputLength = convert( input, 0, input.length,
                                        output, 0, output.length );
            outputLength += flush( output, outputLength, output.length );
            char [] returnedOutput = new char[ outputLength ];
            System.arraycopy( output, 0, returnedOutput, 0, outputLength );
            return returnedOutput;
        }
        catch( ConversionBufferFullException e ) {
            throw new
                InternalError("this.getMaxCharsBerByte returned bad value");
        }
        catch( UnknownCharacterException e ) {
            throw new InternalError();
        }
        finally {
            subMode = savedSubMode;
        }
    }
    public abstract int flush( char[] output, int outStart, int outEnd )
        throws MalformedInputException, ConversionBufferFullException;
    public abstract void reset();
    public int getMaxCharsPerByte() {
        return 1;
    }
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
    public void setSubstitutionChars(char[] c)
        throws IllegalArgumentException
    {
        if( c.length > getMaxCharsPerByte() ) {
            throw new IllegalArgumentException();
        }
        subChars = new char[ c.length ];
        System.arraycopy( c, 0, subChars, 0, c.length );
    }
    public String toString() {
        return "ByteToCharConverter: " + getCharacterEncoding();
    }
}
