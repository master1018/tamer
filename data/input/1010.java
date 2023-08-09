public class Bug4858517 {
    static Locale[] locales2Test = new Locale[] {
        new Locale("en"),
        new Locale("de"),
        new Locale("es"),
        new Locale("fr"),
        new Locale("it"),
        new Locale("ja"),
        new Locale("ko"),
        new Locale("sv"),
        new Locale("zh","CN"),
        new Locale("zh","TW")
        };
    public static void main(String[] args) {
        Locale tzLocale;
        for (int i = 0; i < locales2Test.length; i++){
            tzLocale = locales2Test[i];
            TimeZone Rothera = TimeZone.getTimeZone("Antarctica/Rothera");
            if (!Rothera.getDisplayName(false, TimeZone.SHORT, tzLocale).equals ("ROTT"))
                throw new RuntimeException("\n" + tzLocale + ": short name, non-daylight time for Rothera should be \"ROTT\"");
            if (!Rothera.getDisplayName(true, TimeZone.SHORT, tzLocale).equals ("ROTST"))
                throw new RuntimeException("\n" + tzLocale + ": short name, daylight time for Rothera should be \"ROTST\"");
            TimeZone IRT = TimeZone.getTimeZone("Iran");
            if (!IRT.getDisplayName(false, TimeZone.SHORT, tzLocale).equals ("IRST"))
                throw new RuntimeException("\n" + tzLocale + ": short name, non-daylight time for IRT should be \"IRST\"");
            if (!IRT.getDisplayName(true, TimeZone.SHORT, tzLocale).equals ("IRDT"))
             throw new RuntimeException("\n" + tzLocale + ": short name, daylight time for IRT should be \"IRDT\"");
        }
   }
}
