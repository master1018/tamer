public abstract class X11InputMethodDescriptor implements InputMethodDescriptor {
    private static Locale locale;
    public X11InputMethodDescriptor() {
        locale = getSupportedLocale();
    }
    public Locale[] getAvailableLocales() {
        Locale[] locales = {locale};
        return locales;
    }
    public boolean hasDynamicLocaleList() {
        return false;
    }
    public synchronized String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
        String name = "System Input Methods";
        if (Locale.getDefault().equals(displayLanguage)) {
            name = Toolkit.getProperty("AWT.HostInputMethodDisplayName", name);
        }
        return name;
    }
    public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }
    public abstract InputMethod createInputMethod() throws Exception;
    static Locale getSupportedLocale() {
        return SunToolkit.getStartupLocale();
    }
}
