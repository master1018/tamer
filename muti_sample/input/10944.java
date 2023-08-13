public class CodePointInputMethodDescriptor implements InputMethodDescriptor {
    public CodePointInputMethodDescriptor() {
    }
    public InputMethod createInputMethod() throws Exception {
        return new CodePointInputMethod();
    }
    public Locale[] getAvailableLocales() {
        Locale[] locales = {
            new Locale("", "", ""), };
        return locales;
    }
    public synchronized String getInputMethodDisplayName(Locale inputLocale,
            Locale displayLanguage) {
        return "CodePoint Input Method";
    }
    public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }
    public boolean hasDynamicLocaleList() {
        return false;
    }
}
