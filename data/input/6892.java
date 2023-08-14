public class SimpleInputMethodWindow
        extends Frame
        implements InputMethodWindow {
    InputContext inputContext = null;
    public SimpleInputMethodWindow(String title, InputContext context) {
        super(title);
        if (context != null) {
            this.inputContext = context;
        }
        setFocusableWindowState(false);
    }
    public void setInputContext(InputContext inputContext) {
        this.inputContext = inputContext;
    }
    public java.awt.im.InputContext getInputContext() {
        if (inputContext != null) {
            return inputContext;
        } else {
            return super.getInputContext();
        }
    }
}
