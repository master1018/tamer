public class AndroidAudioFileReader extends AudioFileReader {
    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException,
            IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public AudioFileFormat getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException,
            IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException,
            IOException {
        return new AndroidAudioInputStream(file.toURL());
    }
    @Override
    public AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        File file = File.createTempFile("javax.sound.sampled-", null);
        file.deleteOnExit();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = new byte[1024];
        int count = stream.read(buffer);
        while (count >= 0) {
            out.write(buffer, 0, count);
            count = stream.read(buffer);
        }
        out.flush();
        out.close();
        return getAudioInputStream(file);
    }
    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException,
            IOException {
        return new AndroidAudioInputStream(url);
    }
}
