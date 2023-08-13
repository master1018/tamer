public class WaveFileReader extends SunFileReader {
    private static final int MAX_READ_LENGTH = 12;
    public static final AudioFileFormat.Type types[] = {
        AudioFileFormat.Type.WAVE
    };
    public WaveFileReader() {
    }
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat aff = getFMT(stream, true);
        stream.reset();
        return aff;
    }
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream urlStream = url.openStream(); 
        AudioFileFormat fileFormat = null;
        try {
            fileFormat = getFMT(urlStream, false);
        } finally {
            urlStream.close();
        }
        return fileFormat;
    }
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = null;
        FileInputStream fis = new FileInputStream(file);       
        try {
            fileFormat = getFMT(fis, false);
        } finally {
            fis.close();
        }
        return fileFormat;
    }
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = getFMT(stream, true); 
        return new AudioInputStream(stream, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream urlStream = url.openStream();  
        AudioFileFormat fileFormat = null;
        try {
            fileFormat = getFMT(urlStream, false);
        } finally {
            if (fileFormat == null) {
                urlStream.close();
            }
        }
        return new AudioInputStream(urlStream, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        FileInputStream fis = new FileInputStream(file); 
        AudioFileFormat fileFormat = null;
        try {
            fileFormat = getFMT(fis, false);
        } finally {
            if (fileFormat == null) {
                fis.close();
            }
        }
        return new AudioInputStream(fis, fileFormat.getFormat(), fileFormat.getFrameLength());
    }
    private AudioFileFormat getFMT(InputStream stream, boolean doReset) throws UnsupportedAudioFileException, IOException {
        int bytesRead;
        int nread = 0;
        int fmt;
        int length = 0;
        int wav_type = 0;
        short channels;
        long sampleRate;
        long avgBytesPerSec;
        short blockAlign;
        int sampleSizeInBits;
        AudioFormat.Encoding encoding = null;
        DataInputStream dis = new DataInputStream( stream );
        if (doReset) {
            dis.mark(MAX_READ_LENGTH);
        }
        int magic = dis.readInt();
        int fileLength = rllong(dis);
        int waveMagic = dis.readInt();
        int totallength;
        if (fileLength <= 0) {
            fileLength = AudioSystem.NOT_SPECIFIED;
            totallength = AudioSystem.NOT_SPECIFIED;
        } else {
            totallength = fileLength + 8;
        }
        if ((magic != WaveFileFormat.RIFF_MAGIC) || (waveMagic != WaveFileFormat.WAVE_MAGIC)) {
            if (doReset) {
                dis.reset();
            }
            throw new UnsupportedAudioFileException("not a WAVE file");
        }
        while(true) {
            try {
                fmt = dis.readInt();
                nread += 4;
                if( fmt==WaveFileFormat.FMT_MAGIC ) {
                    break;
                } else {
                    length = rllong(dis);
                    nread += 4;
                    if (length % 2 > 0) length++;
                    nread += dis.skipBytes(length);
                }
            } catch (EOFException eof) {
                throw new UnsupportedAudioFileException("Not a valid WAV file");
            }
        }
        length = rllong(dis);
        nread += 4;
        int endLength = nread + length;
        wav_type = rlshort(dis); nread += 2;
        if (wav_type == WaveFileFormat.WAVE_FORMAT_PCM)
            encoding = AudioFormat.Encoding.PCM_SIGNED;  
        else if ( wav_type == WaveFileFormat.WAVE_FORMAT_ALAW )
            encoding = AudioFormat.Encoding.ALAW;
        else if ( wav_type == WaveFileFormat.WAVE_FORMAT_MULAW )
            encoding = AudioFormat.Encoding.ULAW;
        else {
            throw new UnsupportedAudioFileException("Not a supported WAV file");
        }
        channels = rlshort(dis); nread += 2;
        sampleRate = rllong(dis); nread += 4;
        avgBytesPerSec = rllong(dis); nread += 4;
        blockAlign = rlshort(dis); nread += 2;
        sampleSizeInBits = (int)rlshort(dis); nread += 2;
        if ((sampleSizeInBits==8) && encoding.equals(AudioFormat.Encoding.PCM_SIGNED))
            encoding = AudioFormat.Encoding.PCM_UNSIGNED;
        if (length % 2 != 0) length += 1;
        if (endLength > nread)
            nread += dis.skipBytes(endLength - nread);
        nread = 0;
        while(true) {
            try{
                int datahdr = dis.readInt();
                nread+=4;
                if (datahdr == WaveFileFormat.DATA_MAGIC) {
                    break;
                } else {
                    int thisLength = rllong(dis); nread += 4;
                    if (thisLength % 2 > 0) thisLength++;
                    nread += dis.skipBytes(thisLength);
                }
            } catch (EOFException eof) {
                throw new UnsupportedAudioFileException("Not a valid WAV file");
            }
        }
        int dataLength = rllong(dis); nread += 4;
        AudioFormat format = new AudioFormat(encoding,
                                             (float)sampleRate,
                                             sampleSizeInBits, channels,
                                             calculatePCMFrameSize(sampleSizeInBits, channels),
                                             (float)sampleRate, false);
        return new WaveFileFormat(AudioFileFormat.Type.WAVE,
                                  totallength,
                                  format,
                                  dataLength / format.getFrameSize());
    }
}
