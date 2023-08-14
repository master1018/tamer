public class InputContextTest {
    public static void main(String[] args) throws Exception {
        Frame frame = new Frame();
        InputContext ic = frame.getInputContext();
        try {
            ic.removeNotify(null);
            throw new Exception("InputContext.removeNotify(null) doesn't throw NullPointerException");
        } catch (Exception e) {
            if (! (e instanceof NullPointerException)) {
                throw new Exception("InputContext.removeNotify(null) throws " + e
                                    + " instead of NullPointerException.");
            }
        }
        try {
            ic.selectInputMethod(null);
            throw new Exception("InputContext.selectInputMethod(null) doesn't throw NullPointerException");
        } catch (Exception e) {
            if (! (e instanceof NullPointerException)) {
                throw new Exception("InputContext.selectInputMethod(null) throws " + e
                                    + " instead of NullPointerException.");
            }
        }
        try {
            ic.selectInputMethod(Locale.JAPANESE);
        } catch (Exception e) {
            throw new Exception("InputContext.selectInputMethod(Locale) throws " + e);
        }
    }
}
