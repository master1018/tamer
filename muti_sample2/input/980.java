public class test {
    public ClientHttpRequest(URL url) throws IOException {
        this(url.openConnection());
    }
}
