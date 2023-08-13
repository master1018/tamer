public class GetOutputStream {
    public static void main (String argv[]) {
        try {
            URL url = new URL("http:
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            OutputStream os = urlConnection.getOutputStream();
            System.out.println("Passed!");
        } catch (Exception ex) {
            if (ex instanceof java.net.ProtocolException) {
                throw new RuntimeException("getOutputStream failure.");
            }
        }
    }
}
