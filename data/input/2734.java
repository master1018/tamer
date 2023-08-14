public class test {
    public FormatReader(URL url) throws IOException {
        this(new InputStreamReader(url.openStream()));
    }
}
