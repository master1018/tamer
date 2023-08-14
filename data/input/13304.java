public class B6463990 {
    public static void main(String[] args) {
        boolean except = false;
        try {
            URLDecoder ud = new java.net.URLDecoder();
            String s = ud.decode("%-1", "iso-8859-1");
            System.out.println((int) s.charAt(0));
        } catch (Exception e) {
            except = true;
        }
        if (!except)
            throw new RuntimeException("IllegalArgumentException not thrown!");
    }
}
