public class bug6277243 {
    public static void main(String[] args) throws Exception {
        Locale root = new Locale("", "", "");
        if (!Locale.ROOT.equals(root)) {
            throw new RuntimeException("Locale.ROOT is not equal to Locale(\"\", \"\", \"\")");
        }
    }
}
