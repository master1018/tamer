public class WaveFloatFileReader extends AudioFileReader {
    public AudioFileFormat getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        stream.mark(200);
        AudioFileFormat format;
        try {
            format = internal_getAudioFileFormat(stream);
        } finally {
            stream.reset();
        }
        return format;
    }
    private AudioFileFormat internal_getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        RIFFReader riffiterator = new RIFFReader(stream);
        if (!riffiterator.getFormat().equals("RIFF"))
            throw new UnsupportedAudioFileException();
        if (!riffiterator.getType().equals("WAVE"))
            throw new UnsupportedAudioFileException();
        boolean fmt_found = false;
        boolean data_found = false;
        int channels = 1;
        long samplerate = 1;
        int framesize = 1;
        int bits = 1;
        while (riffiterator.hasNextChunk()) {
            RIFFReader chunk = riffiterator.nextChunk();
            if (chunk.getFormat().equals("fmt ")) {
                fmt_found = true;
                int format = chunk.readUnsignedShort();
                if (format != 3) 
                    throw new UnsupportedAudioFileException();
                channels = chunk.readUnsignedShort();
                samplerate = chunk.readUnsignedInt();
                chunk.readUnsignedInt();
                framesize = chunk.readUnsignedShort();
                bits = chunk.readUnsignedShort();
            }
            if (chunk.getFormat().equals("data")) {
                data_found = true;
                break;
            }
        }
        if (!fmt_found)
            throw new UnsupportedAudioFileException();
        if (!data_found)
            throw new UnsupportedAudioFileException();
        AudioFormat audioformat = new AudioFormat(
                Encoding.PCM_FLOAT, samplerate, bits, channels,
                framesize, samplerate, false);
        AudioFileFormat fileformat = new AudioFileFormat(
                AudioFileFormat.Type.WAVE, audioformat,
                AudioSystem.NOT_SPECIFIED);
        return fileformat;
    }
    public AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        AudioFileFormat format = getAudioFileFormat(stream);
        RIFFReader riffiterator = new RIFFReader(stream);
        if (!riffiterator.getFormat().equals("RIFF"))
            throw new UnsupportedAudioFileException();
        if (!riffiterator.getType().equals("WAVE"))
            throw new UnsupportedAudioFileException();
        while (riffiterator.hasNextChunk()) {
            RIFFReader chunk = riffiterator.nextChunk();
            if (chunk.getFormat().equals("data")) {
                return new AudioInputStream(chunk, format.getFormat(),
                        chunk.getSize());
            }
        }
        throw new UnsupportedAudioFileException();
    }
    public AudioFileFormat getAudioFileFormat(URL url)
            throws UnsupportedAudioFileException, IOException {
        InputStream stream = url.openStream();
        AudioFileFormat format;
        try {
            format = getAudioFileFormat(new BufferedInputStream(stream));
        } finally {
            stream.close();
        }
        return format;
    }
    public AudioFileFormat getAudioFileFormat(File file)
            throws UnsupportedAudioFileException, IOException {
        InputStream stream = new FileInputStream(file);
        AudioFileFormat format;
        try {
            format = getAudioFileFormat(new BufferedInputStream(stream));
        } finally {
            stream.close();
        }
        return format;
    }
    public AudioInputStream getAudioInputStream(URL url)
            throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(new BufferedInputStream(url.openStream()));
    }
    public AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(new BufferedInputStream(new FileInputStream(
                file)));
    }
}
