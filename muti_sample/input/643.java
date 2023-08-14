public class WindowType {
    private static void test(Window window, Window.Type type) {
        window.setType(type);
        window.setVisible(true);
        Util.waitForIdle(null);
        window.setVisible(false);
    }
    private static void test(Window.Type type) {
        test(new Window((Frame)null), type);
        test(new Frame(), type);
        test(new Dialog((Frame)null), type);
    }
    public static void main(String[] args) {
        test(Window.Type.NORMAL);
        test(Window.Type.UTILITY);
        test(Window.Type.POPUP);
    }
}
