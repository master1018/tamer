public class B6827999
{
    public static void main(String[] args) throws Exception {
        URL[] urls = new URL[] {new URL("http:
        MyURLClassLoader ucl = new MyURLClassLoader(urls);
        ucl.addURL(new URL("http:
        urls = ucl.getURLs();
        if (urls.length != 2)
            throw new RuntimeException("Failed:(1)");
        ucl.close();
        ucl.addURL(new URL("http:
        if (ucl.getURLs().length != 2) {
            throw new RuntimeException("Failed:(2)");
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
