final class NormalizerDataReader implements ICUBinary.Authenticate {
    protected NormalizerDataReader(InputStream inputStream)
                                        throws IOException{
        unicodeVersion = ICUBinary.readHeader(inputStream, DATA_FORMAT_ID, this);
        dataInputStream = new DataInputStream(inputStream);
    }
    protected int[] readIndexes(int length)throws IOException{
        int[] indexes = new int[length];
        for (int i = 0; i <length ; i++) {
             indexes[i] = dataInputStream.readInt();
        }
        return indexes;
    }
    protected void read(byte[] normBytes, byte[] fcdBytes, byte[] auxBytes,
                        char[] extraData, char[] combiningTable)
                        throws IOException{
         dataInputStream.readFully(normBytes);
         for(int i=0;i<extraData.length;i++){
             extraData[i]=dataInputStream.readChar();
         }
         for(int i=0; i<combiningTable.length; i++){
             combiningTable[i]=dataInputStream.readChar();
         }
         dataInputStream.readFully(fcdBytes);
        dataInputStream.readFully(auxBytes);
    }
    public byte[] getDataFormatVersion(){
        return DATA_FORMAT_VERSION;
    }
    public boolean isDataVersionAcceptable(byte version[])
    {
        return version[0] == DATA_FORMAT_VERSION[0]
               && version[2] == DATA_FORMAT_VERSION[2]
               && version[3] == DATA_FORMAT_VERSION[3];
    }
    public byte[] getUnicodeVersion(){
        return unicodeVersion;
    }
    private DataInputStream dataInputStream;
    private byte[] unicodeVersion;
    private static final byte DATA_FORMAT_ID[] = {(byte)0x4E, (byte)0x6F,
                                                    (byte)0x72, (byte)0x6D};
    private static final byte DATA_FORMAT_VERSION[] = {(byte)0x2, (byte)0x2,
                                                        (byte)0x5, (byte)0x2};
}
