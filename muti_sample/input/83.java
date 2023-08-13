public class FrameMinimizeTest {
    public static void main(String args[]) throws Exception {
        Frame frame = new Frame("Frame Minimize Test");
        Button b = new Button("Focus ownder");
        frame.add("South", b);
        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(null);
        if (!b.hasFocus()) {
            throw new RuntimeException("button is not a focus owner after showing :(");
        }
        frame.setExtendedState(Frame.ICONIFIED);
        Util.waitForIdle(null);
        frame.setExtendedState(Frame.NORMAL);
        Util.waitForIdle(null);
        if (!b.hasFocus()) {
            throw new RuntimeException("button is not a focus owner after restoring :(");
        }
    }
}
