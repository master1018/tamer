public class CollatorProviderImpl extends CollatorProvider {
    static Locale[] avail = {
        Locale.JAPAN,
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        new Locale("xx", "YY", "ZZZZ")};
    static String[] dialect = {
        "\u3067\u3059\u3002",
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "-xx-YY-ZZZZ"
    };
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public Collator getInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                RuleBasedCollator ja = (RuleBasedCollator)Collator.getInstance(Locale.JAPANESE);
                try {
                    return new RuleBasedCollator(ja.getRules()+"& Z < "+dialect[i]);
                } catch (ParseException pe) {
System.err.println(pe+ja.getRules()+"& Z < "+dialect[i]);
                    return ja;
                }
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
}
