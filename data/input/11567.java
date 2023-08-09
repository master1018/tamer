public class Bug6572242 {
    public static void main(String[] args) {
        ResourceBundle rb = ResourceBundle.getBundle("bug6572242");
        String data = rb.getString("data");
        if (!data.equals("type")) {
            throw new RuntimeException("got \"" + data + "\", expected \"type\"");
        }
    }
}
