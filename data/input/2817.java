public class test {
    protected URLConnection openConnection(URL url) {
        return new JMSURLConnection(url);
    }
}
