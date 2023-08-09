public final strictfp class Bug6355009 {
    private final ResourceBundle bundle = ResourceBundle.getBundle(Bug6355009.class.getName());
    public final static void main(String[] args) {
        try {
            new Bug6355009();
        } catch (MissingResourceException e) {
            Throwable cause = e;
            int count = 0;
            while ((cause = cause.getCause()) != null) {
                if (cause instanceof MissingResourceException) {
                    count++;
                }
            }
            if (count > 0) {
                throw new RuntimeException("too many causes: " + count);
            }
        }
    }
}
