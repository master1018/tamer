public class InputContext {
    protected InputContext() {
    }
    public static InputContext getInstance() {
        return new InputMethodContext();
    }
    public void dispatchEvent(AWTEvent event) {
    }
    public void dispose() {
    }
    public void endComposition() {
    }
    public Object getInputMethodControlObject() {
        return null;
    }
    public Locale getLocale() {
        return null;
    }
    public boolean isCompositionEnabled() {
        return false;
    }
    public void reconvert() {
    }
    public boolean selectInputMethod(Locale locale) {
        return false;
    }
    public void setCharacterSubsets(Character.Subset[] subsets) {
    }
    public void setCompositionEnabled(boolean enable) {
    }
}
