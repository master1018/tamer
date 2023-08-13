public class URLEncoderEncodeArgs {
    public static void main (String[] args) {
        try {
            String s1 = URLEncoder.encode ("Hello World", null);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException ("NPE should have been thrown");
        } catch (NullPointerException e) {
            try {
                String s2 = URLEncoder.encode ("Hello World", "");
            } catch (UnsupportedEncodingException ee) {
                return;
            }
            throw new RuntimeException ("empty string was accepted as encoding name");
        }
        throw new RuntimeException ("null reference was accepted as encoding name");
    }
}
