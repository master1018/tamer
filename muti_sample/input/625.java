abstract class SunFileWriter extends AudioFileWriter {
    protected static final int bufferSize = 16384;
    protected static final int bisBufferSize = 4096;
    final AudioFileFormat.Type types[];
    SunFileWriter(AudioFileFormat.Type types[]) {
        this.types = types;
    }
    public AudioFileFormat.Type[] getAudioFileTypes(){
        AudioFileFormat.Type[] localArray = new AudioFileFormat.Type[types.length];
        System.arraycopy(types, 0, localArray, 0, types.length);
        return localArray;
    }
    public abstract AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream);
    public abstract int write(AudioInputStream stream, AudioFileFormat.Type fileType, OutputStream out) throws IOException;
    public abstract int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException;
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
    protected class NoCloseInputStream extends InputStream {
        private final InputStream in;
        public NoCloseInputStream(InputStream in) {
            this.in = in;
        }
        @Override
        public int read() throws IOException {
            return in.read();
        }
        @Override
        public int read(byte b[]) throws IOException {
            return in.read(b);
        }
        @Override
        public int read(byte b[], int off, int len) throws IOException {
            return in.read(b, off, len);
        }
        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }
        @Override
        public int available() throws IOException {
            return in.available();
        }
        @Override
        public void close() throws IOException {
        }
        @Override
        public void mark(int readlimit) {
            in.mark(readlimit);
        }
        @Override
        public void reset() throws IOException {
            in.reset();
        }
        @Override
        public boolean markSupported() {
            return in.markSupported();
        }
    }
}
