public class FrameLocation {
    private static final int X = 250;
    private static final int Y = 250;
    public static void main(String[] args) {
        Frame f = new Frame("test");
        f.setBounds(X, Y, 250, 250); 
        f.setVisible(true);
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            int x = f.getX();
            int y = f.getY();
            if (x != X || y != Y) {
                throw new RuntimeException("The frame location is wrong! Current: " + x + ", " + y + ";  expected: " + X + ", " + Y);
            }
            synchronized (f.getTreeLock()) {
                f.removeNotify();
                f.addNotify();
            }
        }
        f.dispose();
    }
}
