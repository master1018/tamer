public class GetContentType {
    static final String JAR_MIME_TYPE = "x-java/jar";
    static final String GIF_MIME_TYPE = "image/gif";
    public static void main(String[] args) throws Exception {
        URL url = new URL(getSpec());
        URLConnection connection = url.openConnection();
        String contentType = connection.getContentType();
        System.out.println(url + " jar content type: " + contentType);
        if (!contentType.equals(JAR_MIME_TYPE)) {
            throw new RuntimeException("invalid MIME type for JAR archive");
        }
        url = new URL(url, "image.gif");
        connection = url.openConnection();
        contentType = connection.getContentType();
        System.out.println(url + " img content type: " + contentType);
        if (!contentType.equals(GIF_MIME_TYPE)) {
            throw new RuntimeException("invalid MIME type for JAR entry");
        }
    }
    static String getSpec() throws IOException {
        File file = new File(".");
        return "jar:file:" + file.getCanonicalPath() +
            File.separator + "jars" + File.separator + "test.jar!/";
    }
}
