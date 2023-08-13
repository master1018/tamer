public class LoadJFrame {
    public static void main(String[] args) {
        new JFrame().show();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }
        System.exit(0);
    }
}
