public class AddURLTest
{
    public static void main(String[] args) throws Exception {
        URL[] urls = new URL[] {new URL("http:
        MyURLClassLoader ucl = new MyURLClassLoader(urls);
        ucl.addURL(null);
        ucl.addURL(new URL("http:
        ucl.addURL(null);
        ucl.addURL(new URL("http:
        ucl.addURL(null);
        ucl.addURL(new URL("http:
        urls = ucl.getURLs();
        if (urls.length != 1)
            throw new RuntimeException(
                "Failed: There should only be 1 url in the list of search URLs");
        URL url;
        for (int i=0; i<urls.length; i++) {
            url = urls[i];
            if (url == null || !url.equals(new URL("http:
                throw new RuntimeException(
                        "Failed: The url should not be null and should be http:
        }
    }
    static class MyURLClassLoader extends URLClassLoader {
        public MyURLClassLoader(URL[] urls) {
            super(urls);
        }
        public void addURL(URL url) {
            super.addURL(url);
        }
    }
}
