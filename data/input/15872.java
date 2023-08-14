public class InputContext {
    protected InputContext() {
    }
    public static InputContext getInstance() {
        return new sun.awt.im.InputMethodContext();
    }
    public boolean selectInputMethod(Locale locale) {
        return false;
    }
    public Locale getLocale() {
        return null;
    }
    public void setCharacterSubsets(Subset[] subsets) {
    }
    public void setCompositionEnabled(boolean enable) {
    }
    @Transient
    public boolean isCompositionEnabled() {
        return false;
    }
    public void reconvert() {
    }
    public void dispatchEvent(AWTEvent event) {
    }
    public void removeNotify(Component client) {
    }
    public void endComposition() {
    }
    public void dispose() {
    }
    public Object getInputMethodControlObject() {
        return null;
    }
}
