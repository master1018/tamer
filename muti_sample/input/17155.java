public class URLParsing {
    public static void main(String[] args) throws Exception {
        File local = new File(System.getProperty("test.src", "."), "jars");
        String path = "jar:file:"
             + local.getPath()
             + "/class_path_test.jar!/Foo.java";
        URL aURL = new URL(path);
        URL testURL = new URL(aURL, "foo/../Foo.java");
        InputStream in = testURL.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String firstLine = reader.readLine();
        if (!firstLine.startsWith("public class Foo {"))
            throw new RuntimeException("Jar or File parsing failure.");
    }
}
