public class LoadFrame {
    public static void main(String[] args) {
        new Frame().show();
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        System.exit(0);
    }
}
