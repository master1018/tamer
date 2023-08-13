public class Bug4210525 {
    public static void main(String[] args) throws Exception {
        String language = "en";
        String country = "US";
        String variant = "socal";
        Locale aLocale = new Locale(language, country, variant);
        String localeVariant = aLocale.getVariant();
        if (localeVariant.equals(variant)) {
            System.out.println("passed");
        } else {
            System.out.println("failed");
            throw new Exception("Bug4210525 test failed.");
        }
    }
}
