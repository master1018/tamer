public class AiffFileReader extends SunFileReader  {
    private static final int MAX_READ_LENGTH = 8;
    public static final AudioFileFormat.Type types[] = {
        AudioFileFormat.Type.AIFF
    };
    public AiffFileReader() {
    }
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat aff = getCOMM(stream, true);
        stream.reset();
        return aff;
    }
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = null;
        InputStream urlStream = url.openStream();       
        try {
            fileFormat = getCOMM(urlStream, false);
        } finally {
            urlStream.close();
        }
        return fileFormat;
    }
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = null;
        FileInputStream fis = new FileInputStream(file);       
        try {
            fileFormat = getCOMM(fis, false);
        } finally {
            fis.close();
        }
        return fileFormat;
    }
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = getCOMM(stream, true);     
        return new AudioInputStream(stream, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream urlStream = url.openStream();  
        AudioFileFormat fileFormat = null;
        try {
            fileFormat = getCOMM(urlStream, false);
        } finally {
            if (fileFormat == null) {
                urlStream.close();
            }
        }
        return new AudioInputStream(urlStream, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    public AudioInputStream getAudioInputStream(File file)
        throws UnsupportedAudioFileException, IOException {
        FileInputStream fis = new FileInputStream(file); 
        AudioFileFormat fileFormat = null;
        try {
            fileFormat = getCOMM(fis, false);
        } finally {
            if (fileFormat == null) {
                fis.close();
            }
        }
        return new AudioInputStream(fis, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    private AudioFileFormat getCOMM(InputStream is, boolean doReset)
        throws UnsupportedAudioFileException, IOException {
        DataInputStream dis = new DataInputStream(is);
        if (doReset) {
            dis.mark(MAX_READ_LENGTH);
        }
        int fileRead = 0;
        int dataLength = 0;
        AudioFormat format = null;
        int magic = dis.readInt();
        if (magic != AiffFileFormat.AIFF_MAGIC) {
            if (doReset) {
                dis.reset();
            }
            throw new UnsupportedAudioFileException("not an AIFF file");
        }
        int length = dis.readInt();
        int iffType = dis.readInt();
        fileRead += 12;
        int totallength;
        if(length <= 0 ) {
            length = AudioSystem.NOT_SPECIFIED;
            totallength = AudioSystem.NOT_SPECIFIED;
        } else {
            totallength = length + 8;
        }
        boolean aifc = false;
        if (iffType ==  AiffFileFormat.AIFC_MAGIC) {
            aifc = true;
        }
        boolean ssndFound = false;
        while (!ssndFound) {
            int chunkName = dis.readInt();
            int chunkLen = dis.readInt();
            fileRead += 8;
            int chunkRead = 0;
            switch (chunkName) {
            case AiffFileFormat.FVER_MAGIC:
                break;
            case AiffFileFormat.COMM_MAGIC:
                if ((!aifc && chunkLen < 18) || (aifc && chunkLen < 22)) {
                    throw new UnsupportedAudioFileException("Invalid AIFF/COMM chunksize");
                }
                int channels = dis.readShort();
                dis.readInt();
                int sampleSizeInBits = dis.readShort();
                float sampleRate = (float) read_ieee_extended(dis);
                chunkRead += (2 + 4 + 2 + 10);
                AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                if (aifc) {
                    int enc = dis.readInt(); chunkRead += 4;
                    switch (enc) {
                    case AiffFileFormat.AIFC_PCM:
                        encoding = AudioFormat.Encoding.PCM_SIGNED;
                        break;
                    case AiffFileFormat.AIFC_ULAW:
                        encoding = AudioFormat.Encoding.ULAW;
                        sampleSizeInBits = 8; 
                        break;
                    default:
                        throw new UnsupportedAudioFileException("Invalid AIFF encoding");
                    }
                }
                int frameSize = calculatePCMFrameSize(sampleSizeInBits, channels);
                format =  new AudioFormat(encoding, sampleRate,
                                          sampleSizeInBits, channels,
                                          frameSize, sampleRate, true);
                break;
            case AiffFileFormat.SSND_MAGIC:
                int dataOffset = dis.readInt();
                int blocksize = dis.readInt();
                chunkRead += 8;
                if (chunkLen < length) {
                    dataLength = chunkLen - chunkRead;
                } else {
                    dataLength = length - (fileRead + chunkRead);
                }
                ssndFound = true;
                break;
            } 
            fileRead += chunkRead;
            if (!ssndFound) {
                int toSkip = chunkLen - chunkRead;
                if (toSkip > 0) {
                    fileRead += dis.skipBytes(toSkip);
                }
            }
        } 
        if (format == null) {
            throw new UnsupportedAudioFileException("missing COMM chunk");
        }
        AudioFileFormat.Type type = aifc?AudioFileFormat.Type.AIFC:AudioFileFormat.Type.AIFF;
        return new AiffFileFormat(type, totallength, format, dataLength / format.getFrameSize());
    }
    private void write_ieee_extended(DataOutputStream dos, double f) throws IOException {
        int exponent = 16398;
        double highMantissa = f;
        while (highMantissa < 44000) {
            highMantissa *= 2;
            exponent--;
        }
        dos.writeShort(exponent);
        dos.writeInt( ((int) highMantissa) << 16);
        dos.writeInt(0); 
    }
    private double read_ieee_extended(DataInputStream dis) throws IOException {
        double f = 0;
        int expon = 0;
        long hiMant = 0, loMant = 0;
        long t1, t2;
        double HUGE = ((double)3.40282346638528860e+38);
        expon = dis.readUnsignedShort();
        t1 = (long)dis.readUnsignedShort();
        t2 = (long)dis.readUnsignedShort();
        hiMant = t1 << 16 | t2;
        t1 = (long)dis.readUnsignedShort();
        t2 = (long)dis.readUnsignedShort();
        loMant = t1 << 16 | t2;
        if (expon == 0 && hiMant == 0 && loMant == 0) {
            f = 0;
        } else {
            if (expon == 0x7FFF)
                f = HUGE;
            else {
                expon -= 16383;
                expon -= 31;
                f = (hiMant * Math.pow(2, expon));
                expon -= 32;
                f += (loMant * Math.pow(2, expon));
            }
        }
        return f;
    }
}
