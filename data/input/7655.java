public class FileURLTest {
    public static void main(String [] args)
    {
        String name = System.getProperty("os.name");
        if (name.startsWith("Windows")) {
            String urlStr = "file:
            try {
                URL url = new URL(urlStr);
                URLConnection urlConnection = url.openConnection();
                InputStream in = urlConnection.getInputStream();
                in.close();
            } catch (IOException e) {
                if (e.getMessage().startsWith("C:\\nonexisted.txt")) {
                    System.out.println("Test passed!");
                } else {
                    throw new RuntimeException("Can't handle '|' in place of ':' in file urls");
                }
            }
        }
    }
}
