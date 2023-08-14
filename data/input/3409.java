public class RemoveAfterRequest {
    final static Frame frame = new Frame("test frame");
    final static Button btn1 = new Button("btn1");
    final static Button btn2 = new Button("btn2");
    final static Button btn3 = new Button("btn3");
    public static void main(String[] args) {
        frame.setLayout(new GridLayout(3, 1));
        frame.add(btn1);
        frame.add(btn2);
        frame.add(btn3);
        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(null);
        if (!btn1.hasFocus()) {
            btn1.requestFocus();
            Util.waitForIdle(null);
            if (!btn1.hasFocus()) {
                throw new TestErrorException("couldn't focus " + btn1);
            }
        }
        if (!Util.trackFocusGained(btn3, new Runnable() {
                public void run() {
                    btn3.requestFocus();
                    frame.remove(btn1);
                    frame.invalidate();
                                frame.validate();
                                frame.repaint();
                }
            }, 2000, true))
        {
            throw new TestFailedException("focus request on removal failed");
        }
        System.out.println("Test passed.");
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class TestErrorException extends RuntimeException {
    TestErrorException(String msg) {
        super("Unexpected error: " + msg);
    }
}
