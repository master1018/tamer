public class ExifContentGuesser {
    public static void main(String args[]) throws Exception {
        String filename = System.getProperty("test.src", ".") +
                          "/" + "olympus.jpg";
        System.out.println("filename: " + filename);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filename));
            String content_type = URLConnection.guessContentTypeFromStream(in);
            if (content_type == null) {
                throw new Exception("Test failed: Could not recognise " +
                                "Exif image");
            } else {
                System.out.println("content-type: " + content_type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
