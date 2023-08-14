public class InputMethodJFrame
        extends JFrame
        implements InputMethodWindow {
    InputContext inputContext = null;
    public InputMethodJFrame(String title, InputContext context) {
        super(title);
        if(JFrame.isDefaultLookAndFeelDecorated())
        {
           this.setUndecorated(true);
           this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        }
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
