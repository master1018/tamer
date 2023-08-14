public class ClearGlobalFocusOwnerTest {
    static volatile boolean isFocusLost = false;
    static Frame frame = new Frame("Test frame");
    static Button button = new Button("Test button");
    public static void main(String[] args) {
        button.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent fe) {
                    if (fe.isTemporary()) {
                        throw new TestFailedException("the FocusLost event is temporary: " + fe);
                    }
                    isFocusLost = true;
                }
            });
        frame.add(button);
        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(null);
        if (!button.hasFocus()) {
            button.requestFocus();
            Util.waitForIdle(null);
            if (!button.hasFocus()) {
                throw new TestErrorException("couldn't focus " + button);
            }
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        Util.waitForIdle(null);
        if (!isFocusLost) {
            throw new TestFailedException("no FocusLost event happened on clearGlobalFocusOwner");
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
