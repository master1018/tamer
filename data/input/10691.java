public final class ICUBinary
{
    public static interface Authenticate
    {
        public boolean isDataVersionAcceptable(byte version[]);
    }
    public static final byte[] readHeader(InputStream inputStream,
                                        byte dataFormatIDExpected[],
                                        Authenticate authenticate)
                                                          throws IOException
    {
        DataInputStream input = new DataInputStream(inputStream);
        char headersize = input.readChar();
        int readcount = 2;
        byte magic1 = input.readByte();
        readcount ++;
        byte magic2 = input.readByte();
        readcount ++;
        if (magic1 != MAGIC1 || magic2 != MAGIC2) {
            throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
        }
        input.readChar(); 
        readcount += 2;
        input.readChar(); 
        readcount += 2;
        byte bigendian    = input.readByte();
        readcount ++;
        byte charset      = input.readByte();
        readcount ++;
        byte charsize     = input.readByte();
        readcount ++;
        input.readByte(); 
        readcount ++;
        byte dataFormatID[] = new byte[4];
        input.readFully(dataFormatID);
        readcount += 4;
        byte dataVersion[] = new byte[4];
        input.readFully(dataVersion);
        readcount += 4;
        byte unicodeVersion[] = new byte[4];
        input.readFully(unicodeVersion);
        readcount += 4;
        if (headersize < readcount) {
            throw new IOException("Internal Error: Header size error");
        }
        input.skipBytes(headersize - readcount);
        if (bigendian != BIG_ENDIAN_ || charset != CHAR_SET_
            || charsize != CHAR_SIZE_
            || !Arrays.equals(dataFormatIDExpected, dataFormatID)
            || (authenticate != null
                && !authenticate.isDataVersionAcceptable(dataVersion))) {
            throw new IOException(HEADER_AUTHENTICATION_FAILED_);
        }
        return unicodeVersion;
    }
    private static final byte MAGIC1 = (byte)0xda;
    private static final byte MAGIC2 = (byte)0x27;
    private static final byte BIG_ENDIAN_ = 1;
    private static final byte CHAR_SET_ = 0;
    private static final byte CHAR_SIZE_ = 2;
    private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ =
                       "ICU data file error: Not an ICU data file";
    private static final String HEADER_AUTHENTICATION_FAILED_ =
        "ICU data file error: Header authentication failed, please check if you have a valid ICU data file";
}
