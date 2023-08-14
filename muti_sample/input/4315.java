public class X11FontPathCrashTest {
    public static void main(String[] args) {
        new Font("nonexistentfont", Font.PLAIN, 12).getFamily();
    }
}
