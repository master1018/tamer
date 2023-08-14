public class TestGetDefScreenDevice {
    public static void main(String[] args) throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            ge.getDefaultScreenDevice();
            throw new Exception("Failed. HeadlessException not thrown");
        } catch (HeadlessException he) {
        }
    }
}
