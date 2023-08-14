final class InputMethodLocator {
    private InputMethodDescriptor descriptor;
    private ClassLoader loader;
    private Locale locale;
    InputMethodLocator(InputMethodDescriptor descriptor, ClassLoader loader, Locale locale) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor can't be null");
        }
        this.descriptor = descriptor;
        this.loader = loader;
        this.locale = locale;
    }
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        InputMethodLocator otherLocator = (InputMethodLocator) other;
        if (!descriptor.getClass().equals(otherLocator.descriptor.getClass())) {
            return false;
        }
        if (loader == null && otherLocator.loader != null
            || loader != null && !loader.equals(otherLocator.loader)) {
            return false;
        }
        if (locale == null && otherLocator.locale != null
            || locale != null && !locale.equals(otherLocator.locale)) {
            return false;
        }
        return true;
    }
    public int hashCode() {
        int result = descriptor.hashCode();
        if (loader != null) {
            result |= loader.hashCode() << 10;
        }
        if (locale != null) {
            result |= locale.hashCode() << 20;
        }
        return result;
    }
    InputMethodDescriptor getDescriptor() {
        return descriptor;
    }
    ClassLoader getClassLoader() {
        return loader;
    }
    Locale getLocale() {
        return locale;
    }
    boolean isLocaleAvailable(Locale locale) {
        try {
            Locale[] locales = descriptor.getAvailableLocales();
            for (int i = 0; i < locales.length; i++) {
                if (locales[i].equals(locale)) {
                    return true;
                }
            }
        } catch (AWTException e) {
        }
        return false;
    }
    InputMethodLocator deriveLocator(Locale forLocale) {
        if (forLocale == locale) {
            return this;
        } else {
            return new InputMethodLocator(descriptor, loader, forLocale);
        }
    }
    boolean sameInputMethod(InputMethodLocator other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!descriptor.getClass().equals(other.descriptor.getClass())) {
            return false;
        }
        if (loader == null && other.loader != null
            || loader != null && !loader.equals(other.loader)) {
            return false;
        }
        return true;
    }
    String getActionCommandString() {
        String inputMethodString = descriptor.getClass().getName();
        if (locale == null) {
            return inputMethodString;
        } else {
            return inputMethodString + "\n" + locale.toString();
        }
    }
}
