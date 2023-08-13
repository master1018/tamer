public class NonUTF8EncodedChar
{
    public static void main(String[] args) {
        try {
            String s = "file:
            System.out.println("URL = "+s);
            URL url = new URL(s);
            URLConnection urlCon = url.openConnection();
            System.out.println("succeeded");
            urlCon.getInputStream();
             System.out.println("succeeded");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (IllegalArgumentException iae) {
            String message = iae.getMessage();
            if (message == null || message.equals("")) {
                System.out.println("No message");
                throw new RuntimeException("Failed: No message in Exception");
            }
        }
    }
}
