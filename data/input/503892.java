public class AndroidAudioInputStream extends AudioInputStream {
    private URL url;
    public AndroidAudioInputStream(URL url) {
        super(null, new AudioFormat(0.0f, 0, 0, false, false), 0);
        this.url = url;
    }
    URL getURL() {
        return url;
    }
}
