class WInputMethodDescriptor implements InputMethodDescriptor {
    public Locale[] getAvailableLocales() {
        Locale[] locales = getAvailableLocalesInternal();
        Locale[] tmp = new Locale[locales.length];
        System.arraycopy(locales, 0, tmp, 0, locales.length);
        return tmp;
    }
    static Locale[] getAvailableLocalesInternal() {
        return getNativeAvailableLocales();
    }
    public boolean hasDynamicLocaleList() {
        return true;
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
    public InputMethod createInputMethod() throws Exception {
        return new WInputMethod();
    }
    private static native Locale[] getNativeAvailableLocales();
}
