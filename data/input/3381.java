public class bug4123285 extends java.applet.Applet {
    public void start() {
        System.out.println("Hello, world!");
        Locale[] systemLocales = null;
        try {
            System.out.println("Default locale = " + Locale.getDefault());
            systemLocales = Locale.getAvailableLocales();
            System.out.println("Found " + systemLocales.length + " locales:");
            Locale[] locales = new Locale[systemLocales.length];
            for (int i = 0; i < locales.length; i++) {
                Locale lowest = null;
                for (int j = 0; j < systemLocales.length; j++) {
                    if (i > 0 && locales[i - 1].toString().compareTo(systemLocales[j].toString()) >= 0)
                       continue;
                    if (lowest == null || systemLocales[j].toString().compareTo(lowest.toString()) < 0)
                       lowest = systemLocales[j];
                }
                locales[i] = lowest;
            }
            for (int i = 0; i < locales.length; i++) {
                if (locales[i].getCountry().length() == 0)
                   System.out.println("    " + locales[i].getDisplayLanguage() + ":");
                else {
                    if (locales[i].getVariant().length() == 0)
                       System.out.println("        " + locales[i].getDisplayCountry());
                    else
                        System.out.println("        " + locales[i].getDisplayCountry() + ", "
                                    + locales[i].getDisplayVariant());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
