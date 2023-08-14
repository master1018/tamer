public class IAEforEmptyFrameTest {
    public static void main(String[] args) {
        JFrame f = null;
        try {
            f = new JFrame("IAEforEmptyFrameTest");
            f.setUndecorated(true);
            f.setBounds(100, 100, 320, 240);
            f.setVisible(true);
            try { Thread.sleep(1000); } catch (Exception z) {}
            f.setBounds(0, 0, 0, 0);
            try { Thread.sleep(1000); } catch (Exception z) {}
            f.dispose();
        } finally {
            f.dispose();
        };
    }
}
