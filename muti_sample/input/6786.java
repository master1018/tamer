public class DateFormatSymbolsProviderImpl extends DateFormatSymbolsProvider {
    static Locale[] avail = {
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        Locale.JAPAN,
        new Locale("yy", "ZZ")
    };
    static List<Locale> availList = Arrays.asList(avail);
    static String[] dialect = {
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "\u3067\u3059\u3002",
        "-yy-ZZ"
    };
    static Map<Locale, FooDateFormatSymbols> symbols = new HashMap<Locale, FooDateFormatSymbols>(4);
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public DateFormatSymbols getInstance(Locale locale) {
        if (!Utils.supportsLocale(availList, locale)) {
            throw new IllegalArgumentException("locale is not supported: "+locale);
        }
        FooDateFormatSymbols fdfs = symbols.get(locale);
        if (fdfs == null) {
            for (int index = 0; index < avail.length; index ++) {
                if (Utils.supportsLocale(avail[index], locale)) {
                    fdfs = new FooDateFormatSymbols(index);
                    symbols.put(locale, fdfs);
                    break;
                }
            }
        }
        return fdfs;
    }
    class FooDateFormatSymbols extends DateFormatSymbols {
        String dialect = "";
        String[] eras = null;
        String[] months = null;
        String[] shortMonths = null;
        String[] weekdays = null;
        String[] shortWeekdays = null;
        String[] ampms = null;
        public FooDateFormatSymbols(int index) {
            super(DateFormatSymbolsProviderImpl.this.avail[index]);
            dialect = DateFormatSymbolsProviderImpl.this.dialect[index];
        }
        public String[] getEras() {
            if (eras == null) {
                eras = super.getEras();
                for (int i = 0; i < eras.length; i++) {
                    eras[i] = eras[i]+dialect;
                }
            }
            return eras;
        }
        public void setEras(String[] newEras) {
            eras = newEras;
        }
        public String[] getMonths() {
            if (months == null) {
                months = super.getMonths();
                for (int i = 0; i < months.length; i++) {
                    months[i] = months[i]+dialect;
                }
            }
            return months;
        }
        public void setMonths(String[] newMonths) {
            months = newMonths;
        }
        public String[] getShortMonths() {
            if (shortMonths == null) {
                shortMonths = super.getShortMonths();
                for (int i = 0; i < shortMonths.length; i++) {
                    shortMonths[i] = shortMonths[i]+dialect;
                }
            }
            return shortMonths;
        }
        public void setShortMonths(String[] newShortMonths) {
            shortMonths = newShortMonths;
        }
        public String[] getWeekdays() {
            if (weekdays == null) {
                weekdays = super.getWeekdays();
                for (int i = 0; i < weekdays.length; i++) {
                    weekdays[i] = weekdays[i]+dialect;
                }
            }
            return weekdays;
        }
        public void setWeekdays(String[] newWeekdays) {
            weekdays = newWeekdays;
        }
        public String[] getShortWeekdays() {
            if (shortWeekdays == null) {
                shortWeekdays = super.getShortWeekdays();
                for (int i = 0; i < shortWeekdays.length; i++) {
                    shortWeekdays[i] = shortWeekdays[i]+dialect;
                }
            }
            return shortWeekdays;
        }
        public void setShortWeekdays(String[] newShortWeekdays) {
            shortWeekdays = newShortWeekdays;
        }
        public String[] getAmPmStrings() {
            if (ampms == null) {
                ampms = super.getAmPmStrings();
                for (int i = 0; i < ampms.length; i++) {
                    ampms[i] = ampms[i]+dialect;
                }
            }
            return ampms;
        }
        public void setAmPmStrings(String[] newAmpms) {
            ampms = newAmpms;
        }
    }
}
