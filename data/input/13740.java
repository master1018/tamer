public class AuFileWriter extends SunFileWriter {
    public final static int UNKNOWN_SIZE=-1;
    private static final AudioFileFormat.Type auTypes[] = {
        AudioFileFormat.Type.AU
    };
    public AuFileWriter() {
        super(auTypes);
    }
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream) {
        AudioFileFormat.Type[] filetypes = new AudioFileFormat.Type[types.length];
        System.arraycopy(types, 0, filetypes, 0, types.length);
        AudioFormat format = stream.getFormat();
        AudioFormat.Encoding encoding = format.getEncoding();
        if( (AudioFormat.Encoding.ALAW.equals(encoding)) ||
            (AudioFormat.Encoding.ULAW.equals(encoding)) ||
            (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) ||
            (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) ) {
            return filetypes;
        }
        return new AudioFileFormat.Type[0];
    }
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, OutputStream out) throws IOException {
        AuFileFormat auFileFormat = (AuFileFormat)getAudioFileFormat(fileType, stream);
        int bytesWritten = writeAuFile(stream, auFileFormat, out);
        return bytesWritten;
    }
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException {
        AuFileFormat auFileFormat = (AuFileFormat)getAudioFileFormat(fileType, stream);
        FileOutputStream fos = new FileOutputStream( out );     
        BufferedOutputStream bos = new BufferedOutputStream( fos, bisBufferSize );
        int bytesWritten = writeAuFile(stream, auFileFormat, bos );
        bos.close();
        if( auFileFormat.getByteLength()== AudioSystem.NOT_SPECIFIED ) {
            RandomAccessFile raf=new RandomAccessFile(out, "rw");
            if (raf.length()<=0x7FFFFFFFl) {
                raf.skipBytes(8);
                raf.writeInt(bytesWritten-AuFileFormat.AU_HEADERSIZE);
            }
            raf.close();
        }
        return bytesWritten;
    }
    private AudioFileFormat getAudioFileFormat(AudioFileFormat.Type type, AudioInputStream stream) {
        AudioFormat format = null;
        AuFileFormat fileFormat = null;
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        AudioFormat streamFormat = stream.getFormat();
        AudioFormat.Encoding streamEncoding = streamFormat.getEncoding();
        float sampleRate;
        int sampleSizeInBits;
        int channels;
        int frameSize;
        float frameRate;
        int fileSize;
        if( !types[0].equals(type) ) {
            throw new IllegalArgumentException("File type " + type + " not supported.");
        }
        if( (AudioFormat.Encoding.ALAW.equals(streamEncoding)) ||
            (AudioFormat.Encoding.ULAW.equals(streamEncoding)) ) {
            encoding = streamEncoding;
            sampleSizeInBits = streamFormat.getSampleSizeInBits();
        } else if ( streamFormat.getSampleSizeInBits()==8 ) {
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits=8;
        } else {
            encoding = AudioFormat.Encoding.PCM_SIGNED;
            sampleSizeInBits=streamFormat.getSampleSizeInBits();
        }
        format = new AudioFormat( encoding,
                                  streamFormat.getSampleRate(),
                                  sampleSizeInBits,
                                  streamFormat.getChannels(),
                                  streamFormat.getFrameSize(),
                                  streamFormat.getFrameRate(),
                                  true);        
        if( stream.getFrameLength()!=AudioSystem.NOT_SPECIFIED ) {
            fileSize = (int)stream.getFrameLength()*streamFormat.getFrameSize() + AuFileFormat.AU_HEADERSIZE;
        } else {
            fileSize = AudioSystem.NOT_SPECIFIED;
        }
        fileFormat = new AuFileFormat( AudioFileFormat.Type.AU,
                                       fileSize,
                                       format,
                                       (int)stream.getFrameLength() );
        return fileFormat;
    }
    private InputStream getFileStream(AuFileFormat auFileFormat, InputStream audioStream) throws IOException {
        AudioFormat format            = auFileFormat.getFormat();
        int magic          = AuFileFormat.AU_SUN_MAGIC;
        int headerSize     = AuFileFormat.AU_HEADERSIZE;
        long dataSize       = auFileFormat.getFrameLength();
        long dataSizeInBytes = (dataSize==AudioSystem.NOT_SPECIFIED)?UNKNOWN_SIZE:dataSize * format.getFrameSize();
        if (dataSizeInBytes>0x7FFFFFFFl) {
            dataSizeInBytes=UNKNOWN_SIZE;
        }
        int encoding_local = auFileFormat.getAuType();
        int sampleRate     = (int)format.getSampleRate();
        int channels       = format.getChannels();
        boolean bigendian      = true;                  
        byte header[] = null;
        ByteArrayInputStream headerStream = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        SequenceInputStream auStream = null;
        AudioFormat audioStreamFormat = null;
        AudioFormat.Encoding encoding = null;
        InputStream codedAudioStream = audioStream;
        codedAudioStream = audioStream;
        if( audioStream instanceof AudioInputStream ) {
            audioStreamFormat = ((AudioInputStream)audioStream).getFormat();
            encoding = audioStreamFormat.getEncoding();
            if( (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) ||
                (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)
                 && bigendian != audioStreamFormat.isBigEndian()) ) {
                codedAudioStream = AudioSystem.getAudioInputStream( new AudioFormat (
                                                                                     AudioFormat.Encoding.PCM_SIGNED,
                                                                                     audioStreamFormat.getSampleRate(),
                                                                                     audioStreamFormat.getSampleSizeInBits(),
                                                                                     audioStreamFormat.getChannels(),
                                                                                     audioStreamFormat.getFrameSize(),
                                                                                     audioStreamFormat.getFrameRate(),
                                                                                     bigendian),
                                                                    (AudioInputStream)audioStream );
            }
        }
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        if (bigendian) {
            dos.writeInt(AuFileFormat.AU_SUN_MAGIC);
            dos.writeInt(headerSize);
            dos.writeInt((int)dataSizeInBytes);
            dos.writeInt(encoding_local);
            dos.writeInt(sampleRate);
            dos.writeInt(channels);
        } else {
            dos.writeInt(AuFileFormat.AU_SUN_INV_MAGIC);
            dos.writeInt(big2little(headerSize));
            dos.writeInt(big2little((int)dataSizeInBytes));
            dos.writeInt(big2little(encoding_local));
            dos.writeInt(big2little(sampleRate));
            dos.writeInt(big2little(channels));
        }
        dos.close();
        header = baos.toByteArray();
        headerStream = new ByteArrayInputStream( header );
        auStream = new SequenceInputStream(headerStream,
                        new NoCloseInputStream(codedAudioStream));
        return auStream;
    }
    private int writeAuFile(InputStream in, AuFileFormat auFileFormat, OutputStream out) throws IOException {
        int bytesRead = 0;
        int bytesWritten = 0;
        InputStream fileStream = getFileStream(auFileFormat, in);
        byte buffer[] = new byte[bisBufferSize];
        int maxLength = auFileFormat.getByteLength();
        while( (bytesRead = fileStream.read( buffer )) >= 0 ) {
            if (maxLength>0) {
                if( bytesRead < maxLength ) {
                    out.write( buffer, 0, (int)bytesRead );
                    bytesWritten += bytesRead;
                    maxLength -= bytesRead;
                } else {
                    out.write( buffer, 0, (int)maxLength );
                    bytesWritten += maxLength;
                    maxLength = 0;
                    break;
                }
            } else {
                out.write( buffer, 0, (int)bytesRead );
                bytesWritten += bytesRead;
            }
        }
        return bytesWritten;
    }
}
