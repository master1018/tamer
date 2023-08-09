public class AuFileReader extends SunFileReader {
    public static final AudioFileFormat.Type types[] = {
        AudioFileFormat.Type.AU
    };
    public AuFileReader() {
    }
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFormat format = null;
        AuFileFormat fileFormat = null;
        int maxReadLength = 28;
        boolean bigendian  = false;
        int magic          = -1;
        int headerSize     = -1;
        int dataSize       = -1;
        int encoding_local = -1;
        int sampleRate     = -1;
        int frameRate      = -1;
        int frameSize      = -1;
        int channels       = -1;
        int sampleSizeInBits = 0;
        int length = 0;
        int nread = 0;
        AudioFormat.Encoding encoding = null;
        DataInputStream dis = new DataInputStream( stream );
        dis.mark(maxReadLength);
        magic = dis.readInt();
        if (! (magic == AuFileFormat.AU_SUN_MAGIC) || (magic == AuFileFormat.AU_DEC_MAGIC) ||
            (magic == AuFileFormat.AU_SUN_INV_MAGIC) || (magic == AuFileFormat.AU_DEC_INV_MAGIC) ) {
            dis.reset();
            throw new UnsupportedAudioFileException("not an AU file");
        }
        if ((magic == AuFileFormat.AU_SUN_MAGIC) || (magic == AuFileFormat.AU_DEC_MAGIC)) {
            bigendian = true;        
        }
        headerSize     = (bigendian==true ? dis.readInt() : rllong(dis) );  nread += 4;
        dataSize       = (bigendian==true ? dis.readInt() : rllong(dis) );  nread += 4;
        encoding_local = (bigendian==true ? dis.readInt() : rllong(dis) );  nread += 4;
        sampleRate     = (bigendian==true ? dis.readInt() : rllong(dis) );  nread += 4;
        channels       = (bigendian==true ? dis.readInt() : rllong(dis) );  nread += 4;
        frameRate = sampleRate;
        switch (encoding_local) {
        case AuFileFormat.AU_ULAW_8:
            encoding = AudioFormat.Encoding.ULAW;
            sampleSizeInBits = 8;
            break;
        case AuFileFormat.AU_ALAW_8:
            encoding = AudioFormat.Encoding.ALAW;
            sampleSizeInBits = 8;
            break;
        case AuFileFormat.AU_LINEAR_8:
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits = 8;
            break;
        case AuFileFormat.AU_LINEAR_16:
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits = 16;
            break;
        case AuFileFormat.AU_LINEAR_24:
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits = 24;
            break;
        case AuFileFormat.AU_LINEAR_32:
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits = 32;
            break;
        default:
                dis.reset();
                throw new UnsupportedAudioFileException("not a valid AU file");
        }
        frameSize = calculatePCMFrameSize(sampleSizeInBits, channels);
        if( dataSize < 0 ) {
            length = AudioSystem.NOT_SPECIFIED;
        } else {
            length = dataSize / frameSize;
        }
        format = new AudioFormat( encoding, (float)sampleRate, sampleSizeInBits,
                                  channels, frameSize, (float)frameRate, bigendian);
        fileFormat = new AuFileFormat( AudioFileFormat.Type.AU, dataSize+headerSize,
                                       format, length);
        dis.reset(); 
        return fileFormat;
    }
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream                             urlStream = null;
        BufferedInputStream             bis = null;
        AudioFileFormat                 fileFormat = null;
        AudioFormat                             format = null;
        urlStream = url.openStream();   
        try {
            bis = new BufferedInputStream( urlStream, bisBufferSize );
            fileFormat = getAudioFileFormat( bis );             
        } finally {
            urlStream.close();
        }
        return fileFormat;
    }
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        FileInputStream                 fis = null;
        BufferedInputStream             bis = null;
        AudioFileFormat                 fileFormat = null;
        AudioFormat                             format = null;
        fis = new FileInputStream( file );      
        try {
            bis = new BufferedInputStream( fis, bisBufferSize );
            fileFormat = getAudioFileFormat( bis );             
        } finally {
            fis.close();
        }
        return fileFormat;
    }
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        DataInputStream dis = null;
        int headerSize;
        AudioFileFormat fileFormat = null;
        AudioFormat format = null;
        fileFormat = getAudioFileFormat( stream ); 
        format = fileFormat.getFormat();
        dis = new DataInputStream(stream);
        dis.readInt(); 
        headerSize     = (format.isBigEndian()==true ? dis.readInt() : rllong(dis) );
        dis.skipBytes( headerSize - 8 );
        return new AudioInputStream(dis, format, fileFormat.getFrameLength());
    }
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream                             urlStream = null;
        BufferedInputStream             bis = null;
        AudioFileFormat                 fileFormat = null;
        urlStream = url.openStream();   
        AudioInputStream result = null;
        try {
            bis = new BufferedInputStream( urlStream, bisBufferSize );
            result = getAudioInputStream( (InputStream)bis );
        } finally {
            if (result == null) {
                urlStream.close();
            }
        }
        return result;
    }
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        FileInputStream                 fis = null;
        BufferedInputStream             bis = null;
        AudioFileFormat                 fileFormat = null;
        fis = new FileInputStream( file );      
        AudioInputStream result = null;
        try {
            bis = new BufferedInputStream( fis, bisBufferSize );
            result = getAudioInputStream( (InputStream)bis );
        } finally {
            if (result == null) {
                fis.close();
            }
        }
        return result;
    }
}
