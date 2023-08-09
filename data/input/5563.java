public class AudioData {
    private static final AudioFormat DEFAULT_FORMAT =
        new AudioFormat(AudioFormat.Encoding.ULAW,
                        8000,   
                        8,      
                        1,      
                        1,      
                        8000,   
                        true ); 
    AudioFormat format;   
    byte buffer[];
    public AudioData(byte buffer[]) {
        this.buffer = buffer;
        this.format = DEFAULT_FORMAT;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer));
            this.format = ais.getFormat();
            ais.close();
        } catch (IOException e) {
        } catch (UnsupportedAudioFileException e1 ) {
        }
    }
    AudioData(AudioFormat format, byte[] buffer) {
        this.format = format;
        this.buffer = buffer;
    }
}
