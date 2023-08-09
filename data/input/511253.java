public class AndroidSequence extends Sequence {
    private URL url;
    public AndroidSequence(URL url) throws InvalidMidiDataException {
        super(0.0f, 1);
        this.url = url;
    }
    URL getURL() {
        return url;
    }
}
