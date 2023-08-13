public class BuddhistCalendar extends GregorianCalendar {
    private static final long serialVersionUID = -8527488697350388578L;
    private static final int BUDDHIST_YEAR_OFFSET = 543;
    public BuddhistCalendar() {
        super();
    }
    public BuddhistCalendar(TimeZone zone) {
        super(zone);
    }
    public BuddhistCalendar(Locale aLocale) {
        super(aLocale);
    }
    public BuddhistCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }
    public boolean equals(Object obj) {
        return obj instanceof BuddhistCalendar
            && super.equals(obj);
    }
    public int hashCode() {
        return super.hashCode() ^ BUDDHIST_YEAR_OFFSET;
    }
    public int get(int field)
    {
        if (field == YEAR) {
            return super.get(field) + yearOffset;
        }
        return super.get(field);
    }
    public void set(int field, int value)
    {
        if (field == YEAR) {
            super.set(field, value - yearOffset);
        } else {
            super.set(field, value);
        }
    }
    public void add(int field, int amount)
    {
        int savedYearOffset = yearOffset;
        yearOffset = 0;
        try {
            super.add(field, amount);
        } finally {
            yearOffset = savedYearOffset;
        }
    }
    public void roll(int field, int amount)
    {
        int savedYearOffset = yearOffset;
        yearOffset = 0;
        try {
            super.roll(field, amount);
        } finally {
            yearOffset = savedYearOffset;
        }
    }
    public String getDisplayName(int field, int style, Locale locale) {
        if (field != ERA) {
            return super.getDisplayName(field, style, locale);
        }
        if (field < 0 || field >= fields.length ||
            style < SHORT || style > LONG) {
            throw new IllegalArgumentException();
        }
        if (locale == null) {
            throw new NullPointerException();
        }
        ResourceBundle rb = LocaleData.getDateFormatData(locale);
        String[] eras = rb.getStringArray(getKey(style));
        return eras[get(field)];
    }
    public Map<String,Integer> getDisplayNames(int field, int style, Locale locale) {
        if (field != ERA) {
            return super.getDisplayNames(field, style, locale);
        }
        if (field < 0 || field >= fields.length ||
            style < ALL_STYLES || style > LONG) {
            throw new IllegalArgumentException();
        }
        if (locale == null) {
            throw new NullPointerException();
        }
        if (style == ALL_STYLES) {
            Map<String,Integer> shortNames = getDisplayNamesImpl(field, SHORT, locale);
            Map<String,Integer> longNames = getDisplayNamesImpl(field, LONG, locale);
            if (shortNames == null) {
                return longNames;
            }
            if (longNames != null) {
                shortNames.putAll(longNames);
            }
            return shortNames;
        }
        return getDisplayNamesImpl(field, style, locale);
    }
    private Map<String,Integer> getDisplayNamesImpl(int field, int style, Locale locale) {
        ResourceBundle rb = LocaleData.getDateFormatData(locale);
        String[] eras = rb.getStringArray(getKey(style));
        Map<String,Integer> map = new HashMap<String,Integer>(4);
        for (int i = 0; i < eras.length; i++) {
            map.put(eras[i], i);
        }
        return map;
    }
    private String getKey(int style) {
        StringBuilder key = new StringBuilder();
        key.append(BuddhistCalendar.class.getName());
        if (style == SHORT) {
            key.append(".short");
        }
        key.append(".Eras");
        return key.toString();
    }
    public int getActualMaximum(int field) {
        int savedYearOffset = yearOffset;
        yearOffset = 0;
        try {
            return super.getActualMaximum(field);
        } finally {
            yearOffset = savedYearOffset;
        }
    }
    public String toString() {
        String s = super.toString();
        if (!isSet(YEAR)) {
            return s;
        }
        final String yearField = "YEAR=";
        int p = s.indexOf(yearField);
        if (p == -1) {
            return s;
        }
        p += yearField.length();
        StringBuilder sb = new StringBuilder(s.substring(0, p));
        while (Character.isDigit(s.charAt(p++)))
            ;
        int year = internalGet(YEAR) + BUDDHIST_YEAR_OFFSET;
        sb.append(year).append(s.substring(p - 1));
        return sb.toString();
    }
    private transient int yearOffset = BUDDHIST_YEAR_OFFSET;
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        yearOffset = BUDDHIST_YEAR_OFFSET;
    }
}
