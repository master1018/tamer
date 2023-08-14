public class CharAt {
    public static void main (String[] args) {
        CharSequence cs = CharBuffer.wrap("foo");
        for (int i = 0; i < cs.length(); i++)
            System.err.print(cs.charAt(i));
        System.err.println();
    }
}
