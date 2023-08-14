public class bug6344646 {
    public static void main(String[] s) {
        if (File.separatorChar != '\\') {
            return;
        }
        Locale.setDefault(new Locale("lt"));
        File f1 = new File("J\u0301");
        File f2 = new File("j\u0301");
        if (f1.hashCode() != f2.hashCode()) {
            throw new RuntimeException("File.hashCode() for \"J\u0301\" and \"j\u0301\" should be the same");
        }
    }
}
