public class TestManagerCostruttore {
    public static void main(String[] args) {
        try {
            Home home = new Home(null);
            home.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("AWT problems");
        }
    }
}
