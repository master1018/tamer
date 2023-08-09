public class Bug6356571 {
    private ResourceBundle bundle = ResourceBundle.getBundle("Bug6356571");
    void check() {
        String id = bundle.getString("id");
        if (!"6356571".equals(id)) {
            throw new RuntimeException("wrong id: " + id);
        }
    }
    public final static void main(String[] args) {
        new Bug6356571().check();
    }
}
