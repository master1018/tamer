public class AiffFileWriter extends SunFileWriter {
    private static final AudioFileFormat.Type aiffTypes[] = {
        AudioFileFormat.Type.AIFF
    };
    public AiffFileWriter() {
        super(aiffTypes);
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
        AiffFileFormat aiffFileFormat = (AiffFileFormat)getAudioFileFormat(fileType, stream);
        if( stream.getFrameLength() == AudioSystem.NOT_SPECIFIED ) {
            throw new IOException("stream length not specified");
        }
        int bytesWritten = writeAiffFile(stream, aiffFileFormat, out);
        return bytesWritten;
    }
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException {
        AiffFileFormat aiffFileFormat = (AiffFileFormat)getAudioFileFormat(fileType, stream);
        FileOutputStream fos = new FileOutputStream( out );     
        BufferedOutputStream bos = new BufferedOutputStream( fos, bisBufferSize );
        int bytesWritten = writeAiffFile(stream, aiffFileFormat, bos );
        bos.close();
        if( aiffFileFormat.getByteLength()== AudioSystem.NOT_SPECIFIED ) {
            int ssndBlockSize           = (aiffFileFormat.getFormat().getChannels() * aiffFileFormat.getFormat().getSampleSizeInBits());
            int aiffLength=bytesWritten;
            int ssndChunkSize=aiffLength-aiffFileFormat.getHeaderSize()+16;
            long dataSize=ssndChunkSize-16;
            int numFrames=(int) (dataSize*8/ssndBlockSize);
            RandomAccessFile raf=new RandomAccessFile(out, "rw");
            raf.skipBytes(4);
            raf.writeInt(aiffLength-8);
            raf.skipBytes(4+aiffFileFormat.getFverChunkSize()+4+4+2);
            raf.writeInt(numFrames);
            raf.skipBytes(2+10+4);
            raf.writeInt(ssndChunkSize-8);
            raf.close();
        }
        return bytesWritten;
    }
    private AudioFileFormat getAudioFileFormat(AudioFileFormat.Type type, AudioInputStream stream) {
        AudioFormat format = null;
        AiffFileFormat fileFormat = null;
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        AudioFormat streamFormat = stream.getFormat();
        AudioFormat.Encoding streamEncoding = streamFormat.getEncoding();
        float sampleRate;
        int sampleSizeInBits;
        int channels;
        int frameSize;
        float frameRate;
        int fileSize;
        boolean convert8to16 = false;
        if( !types[0].equals(type) ) {
            throw new IllegalArgumentException("File type " + type + " not supported.");
        }
        if( (AudioFormat.Encoding.ALAW.equals(streamEncoding)) ||
            (AudioFormat.Encoding.ULAW.equals(streamEncoding)) ) {
            if( streamFormat.getSampleSizeInBits()==8 ) {
                encoding = AudioFormat.Encoding.PCM_SIGNED;
                sampleSizeInBits=16;
                convert8to16 = true;
            } else {
                throw new IllegalArgumentException("Encoding " + streamEncoding + " supported only for 8-bit data.");
            }
        } else if ( streamFormat.getSampleSizeInBits()==8 ) {
            encoding = AudioFormat.Encoding.PCM_UNSIGNED;
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
            if( convert8to16 ) {
                fileSize = (int)stream.getFrameLength()*streamFormat.getFrameSize()*2 + AiffFileFormat.AIFF_HEADERSIZE;
            } else {
                fileSize = (int)stream.getFrameLength()*streamFormat.getFrameSize() + AiffFileFormat.AIFF_HEADERSIZE;
            }
        } else {
            fileSize = AudioSystem.NOT_SPECIFIED;
        }
        fileFormat = new AiffFileFormat( AudioFileFormat.Type.AIFF,
                                         fileSize,
                                         format,
                                         (int)stream.getFrameLength() );
        return fileFormat;
    }
    private int writeAiffFile(InputStream in, AiffFileFormat aiffFileFormat, OutputStream out) throws IOException {
        int bytesRead = 0;
        int bytesWritten = 0;
        InputStream fileStream = getFileStream(aiffFileFormat, in);
        byte buffer[] = new byte[bisBufferSize];
        int maxLength = aiffFileFormat.getByteLength();
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
    private InputStream getFileStream(AiffFileFormat aiffFileFormat, InputStream audioStream) throws IOException  {
        AudioFormat format = aiffFileFormat.getFormat();
        AudioFormat streamFormat = null;
        AudioFormat.Encoding encoding = null;
        int headerSize          = aiffFileFormat.getHeaderSize();
        int fverChunkSize       = aiffFileFormat.getFverChunkSize();
        int commChunkSize       = aiffFileFormat.getCommChunkSize();
        int aiffLength          = -1;
        int ssndChunkSize       = -1;
        int ssndOffset                  = aiffFileFormat.getSsndChunkOffset();
        short channels = (short) format.getChannels();
        short sampleSize = (short) format.getSampleSizeInBits();
        int ssndBlockSize               = (channels * sampleSize);
        int numFrames                   = aiffFileFormat.getFrameLength();
        long dataSize            = -1;
        if( numFrames != AudioSystem.NOT_SPECIFIED) {
            dataSize = (long) numFrames * ssndBlockSize / 8;
            ssndChunkSize = (int)dataSize + 16;
            aiffLength = (int)dataSize+headerSize;
        }
        float sampleFramesPerSecond = format.getSampleRate();
        int compCode = AiffFileFormat.AIFC_PCM;
        byte header[] = null;
        ByteArrayInputStream headerStream = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        SequenceInputStream aiffStream = null;
        InputStream codedAudioStream = audioStream;
        if( audioStream instanceof AudioInputStream ) {
            streamFormat = ((AudioInputStream)audioStream).getFormat();
            encoding = streamFormat.getEncoding();
            if( (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) ||
                ( (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) && !streamFormat.isBigEndian() ) ) {
                codedAudioStream = AudioSystem.getAudioInputStream( new AudioFormat (
                                                                                     AudioFormat.Encoding.PCM_SIGNED,
                                                                                     streamFormat.getSampleRate(),
                                                                                     streamFormat.getSampleSizeInBits(),
                                                                                     streamFormat.getChannels(),
                                                                                     streamFormat.getFrameSize(),
                                                                                     streamFormat.getFrameRate(),
                                                                                     true ),
                                                                    (AudioInputStream)audioStream );
            } else if( (AudioFormat.Encoding.ULAW.equals(encoding)) ||
                       (AudioFormat.Encoding.ALAW.equals(encoding)) ) {
                if( streamFormat.getSampleSizeInBits() != 8 ) {
                    throw new IllegalArgumentException("unsupported encoding");
                }
                codedAudioStream = AudioSystem.getAudioInputStream( new AudioFormat (
                                                                                     AudioFormat.Encoding.PCM_SIGNED,
                                                                                     streamFormat.getSampleRate(),
                                                                                     streamFormat.getSampleSizeInBits() * 2,
                                                                                     streamFormat.getChannels(),
                                                                                     streamFormat.getFrameSize() * 2,
                                                                                     streamFormat.getFrameRate(),
                                                                                     true ),
                                                                    (AudioInputStream)audioStream );
            }
        }
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeInt(AiffFileFormat.AIFF_MAGIC);
        dos.writeInt( (aiffLength-8) );
        dos.writeInt(AiffFileFormat.AIFF_MAGIC2);
        dos.writeInt(AiffFileFormat.COMM_MAGIC);
        dos.writeInt( (commChunkSize-8) );
        dos.writeShort(channels);
        dos.writeInt(numFrames);
        dos.writeShort(sampleSize);
        write_ieee_extended(dos, sampleFramesPerSecond);   
        dos.writeInt(AiffFileFormat.SSND_MAGIC);
        dos.writeInt( (ssndChunkSize-8) );
        dos.writeInt(0);        
        dos.writeInt(0);        
        dos.close();
        header = baos.toByteArray();
        headerStream = new ByteArrayInputStream( header );
        aiffStream = new SequenceInputStream(headerStream,
                            new NoCloseInputStream(codedAudioStream));
        return aiffStream;
    }
    private static final int DOUBLE_MANTISSA_LENGTH = 52;
    private static final int DOUBLE_EXPONENT_LENGTH = 11;
    private static final long DOUBLE_SIGN_MASK     = 0x8000000000000000L;
    private static final long DOUBLE_EXPONENT_MASK = 0x7FF0000000000000L;
    private static final long DOUBLE_MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;
    private static final int DOUBLE_EXPONENT_OFFSET = 1023;
    private static final int EXTENDED_EXPONENT_OFFSET = 16383;
    private static final int EXTENDED_MANTISSA_LENGTH = 63;
    private static final int EXTENDED_EXPONENT_LENGTH = 15;
    private static final long EXTENDED_INTEGER_MASK = 0x8000000000000000L;
    private void write_ieee_extended(DataOutputStream dos, float f) throws IOException {
        long doubleBits = Double.doubleToLongBits((double) f);
        long sign = (doubleBits & DOUBLE_SIGN_MASK)
            >> (DOUBLE_EXPONENT_LENGTH + DOUBLE_MANTISSA_LENGTH);
        long doubleExponent = (doubleBits & DOUBLE_EXPONENT_MASK)
            >> DOUBLE_MANTISSA_LENGTH;
        long doubleMantissa = doubleBits & DOUBLE_MANTISSA_MASK;
        long extendedExponent = doubleExponent - DOUBLE_EXPONENT_OFFSET
            + EXTENDED_EXPONENT_OFFSET;
        long extendedMantissa = doubleMantissa
            << (EXTENDED_MANTISSA_LENGTH - DOUBLE_MANTISSA_LENGTH);
        long extendedSign = sign << EXTENDED_EXPONENT_LENGTH;
        short extendedBits79To64 = (short) (extendedSign | extendedExponent);
        long extendedBits63To0 = EXTENDED_INTEGER_MASK | extendedMantissa;
        dos.writeShort(extendedBits79To64);
        dos.writeLong(extendedBits63To0);
    }
}
