public class Bug4168625Class implements Bug4168625Getter {
    public ResourceBundle getResourceBundle(String resource, Locale locale) {
        try {
            return ResourceBundle.getBundle(resource, locale);
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
