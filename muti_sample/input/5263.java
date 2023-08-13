public class Bug4152725 {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("expected locale needs to be specified");
        }
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        String localeID = null;
        if (variant.length() > 0) {
            localeID = language + "_" + country + "_" + variant;
        } else if (country.length() > 0) {
            localeID = language + "_" + country;
        } else {
            localeID = language;
        }
        if (localeID.equals(args[0])) {
            System.out.println("Correctly set from command line: " + localeID);
        } else {
            throw new RuntimeException("expected default locale: " + args[0]
                    + ", actual default locale: " + localeID);
        }
    }
}
