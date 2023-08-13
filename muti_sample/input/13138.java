public class Test4206507 {
    public static void main(String[] args) throws UnsupportedEncodingException {
        Locale l = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            byte[] b = "".getBytes("ISO8859-9");
        } finally {
            Locale.setDefault(l);
        }
    }
}
