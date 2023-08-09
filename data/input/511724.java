public class Support_Locale {
    public static boolean areLocalesAvailable(Locale... requiredLocales) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Set<Locale> localeSet = new HashSet<Locale>(Arrays.asList(availableLocales));
        for (Locale requiredLocale : requiredLocales) {
            if (!localeSet.contains(requiredLocale)) {
                return false;
            }
        }
        return true;
    }
}