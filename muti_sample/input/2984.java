public class Bug4179766Class implements Bug4179766Getter {
    public ResourceBundle getResourceBundle(String resource) {
        try {
            return ResourceBundle.getBundle(resource);
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
