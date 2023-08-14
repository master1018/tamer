public class LocaleFamilyNames {
    public static void main(String[] args) throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] all_fonts = ge.getAllFonts();
        Locale[] all_locales = Locale.getAvailableLocales();
        HashSet all_families = new HashSet();
        for (int i=0; i<all_fonts.length; i++) {
            all_families.add(all_fonts[i].getFamily());
            for (int j=0; j<all_locales.length;j++) {
              all_families.add(all_fonts[i].getFamily(all_locales[j]));
            }
        }
        for (int i=0; i<all_locales.length; i++) {
            String[] families_for_locale =
                 ge.getAvailableFontFamilyNames(all_locales[i]);
            for (int j=0; j<families_for_locale.length; j++) {
                if ( !all_families.contains(families_for_locale[j]) ) {
                    System.out.println("LOCALE: [" + all_locales[i]+"]");
                    System.out.print("NO FONT HAS " +
                                       "THE FOLLOWING FAMILY NAME:");
                    System.out.println("["+families_for_locale[j]+"]");
                    throw new Exception("test failed");
                }
            }
        }
    }
}
