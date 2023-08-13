abstract class SunFileReader extends AudioFileReader {
    protected static final int bisBufferSize = 4096;
    public SunFileReader() {
    }
    abstract public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException;
    abstract public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException;
    abstract public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException;
    abstract public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException;
    abstract public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException;
    abstract public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException;
    protected int rllong(DataInputStream dis) throws IOException {
        int b1, b2, b3, b4 ;
        int i = 0;
        i = dis.readInt();
        b1 = ( i & 0xFF ) << 24 ;
        b2 = ( i & 0xFF00 ) << 8;
        b3 = ( i & 0xFF0000 ) >> 8;
        b4 = ( i & 0xFF000000 ) >>> 24;
        i = ( b1 | b2 | b3 | b4 );
        return i;
    }
    protected int big2little(int i) {
        int b1, b2, b3, b4 ;
        b1 = ( i & 0xFF ) << 24 ;
        b2 = ( i & 0xFF00 ) << 8;
        b3 = ( i & 0xFF0000 ) >> 8;
        b4 = ( i & 0xFF000000 ) >>> 24;
        i = ( b1 | b2 | b3 | b4 );
        return i;
    }
    protected short rlshort(DataInputStream dis)  throws IOException {
        short s=0;
        short high, low;
        s = dis.readShort();
        high = (short)(( s & 0xFF ) << 8) ;
        low = (short)(( s & 0xFF00 ) >>> 8);
        s = (short)( high | low );
        return s;
    }
    protected short big2littleShort(short i) {
        short high, low;
        high = (short)(( i & 0xFF ) << 8) ;
        low = (short)(( i & 0xFF00 ) >>> 8);
        i = (short)( high | low );
        return i;
    }
        protected static int calculatePCMFrameSize(int sampleSizeInBits,
                                                                                        int channels) {
                return ((sampleSizeInBits + 7) / 8) * channels;
        }
}
