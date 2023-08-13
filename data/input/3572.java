public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
    public final static int ERA = 0;
    public final static int YEAR = 1;
    public final static int MONTH = 2;
    public final static int WEEK_OF_YEAR = 3;
    public final static int WEEK_OF_MONTH = 4;
    public final static int DATE = 5;
    public final static int DAY_OF_MONTH = 5;
    public final static int DAY_OF_YEAR = 6;
    public final static int DAY_OF_WEEK = 7;
    public final static int DAY_OF_WEEK_IN_MONTH = 8;
    public final static int AM_PM = 9;
    public final static int HOUR = 10;
    public final static int HOUR_OF_DAY = 11;
    public final static int MINUTE = 12;
    public final static int SECOND = 13;
    public final static int MILLISECOND = 14;
    public final static int ZONE_OFFSET = 15;
    public final static int DST_OFFSET = 16;
    public final static int FIELD_COUNT = 17;
    public final static int SUNDAY = 1;
    public final static int MONDAY = 2;
    public final static int TUESDAY = 3;
    public final static int WEDNESDAY = 4;
    public final static int THURSDAY = 5;
    public final static int FRIDAY = 6;
    public final static int SATURDAY = 7;
    public final static int JANUARY = 0;
    public final static int FEBRUARY = 1;
    public final static int MARCH = 2;
    public final static int APRIL = 3;
    public final static int MAY = 4;
    public final static int JUNE = 5;
    public final static int JULY = 6;
    public final static int AUGUST = 7;
    public final static int SEPTEMBER = 8;
    public final static int OCTOBER = 9;
    public final static int NOVEMBER = 10;
    public final static int DECEMBER = 11;
    public final static int UNDECIMBER = 12;
    public final static int AM = 0;
    public final static int PM = 1;
    public static final int ALL_STYLES = 0;
    public static final int SHORT = 1;
    public static final int LONG = 2;
    protected int           fields[];
    protected boolean       isSet[];
    transient private int   stamp[];
    protected long          time;
    protected boolean       isTimeSet;
    protected boolean       areFieldsSet;
    transient boolean       areAllFieldsSet;
    private boolean         lenient = true;
    private TimeZone        zone;
    transient private boolean sharedZone = false;
    private int             firstDayOfWeek;
    private int             minimalDaysInFirstWeek;
    private static final ConcurrentMap<Locale, int[]> cachedLocaleData
        = new ConcurrentHashMap<Locale, int[]>(3);
    private static final int        UNSET = 0;
    private static final int        COMPUTED = 1;
    private static final int        MINIMUM_USER_STAMP = 2;
    static final int ALL_FIELDS = (1 << FIELD_COUNT) - 1;
    private int             nextStamp = MINIMUM_USER_STAMP;
    static final int        currentSerialVersion = 1;
    private int             serialVersionOnStream = currentSerialVersion;
    static final long       serialVersionUID = -1807547505821590642L;
    final static int ERA_MASK           = (1 << ERA);
    final static int YEAR_MASK          = (1 << YEAR);
    final static int MONTH_MASK         = (1 << MONTH);
    final static int WEEK_OF_YEAR_MASK  = (1 << WEEK_OF_YEAR);
    final static int WEEK_OF_MONTH_MASK = (1 << WEEK_OF_MONTH);
    final static int DAY_OF_MONTH_MASK  = (1 << DAY_OF_MONTH);
    final static int DATE_MASK          = DAY_OF_MONTH_MASK;
    final static int DAY_OF_YEAR_MASK   = (1 << DAY_OF_YEAR);
    final static int DAY_OF_WEEK_MASK   = (1 << DAY_OF_WEEK);
    final static int DAY_OF_WEEK_IN_MONTH_MASK  = (1 << DAY_OF_WEEK_IN_MONTH);
    final static int AM_PM_MASK         = (1 << AM_PM);
    final static int HOUR_MASK          = (1 << HOUR);
    final static int HOUR_OF_DAY_MASK   = (1 << HOUR_OF_DAY);
    final static int MINUTE_MASK        = (1 << MINUTE);
    final static int SECOND_MASK        = (1 << SECOND);
    final static int MILLISECOND_MASK   = (1 << MILLISECOND);
    final static int ZONE_OFFSET_MASK   = (1 << ZONE_OFFSET);
    final static int DST_OFFSET_MASK    = (1 << DST_OFFSET);
    protected Calendar()
    {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        sharedZone = true;
    }
    protected Calendar(TimeZone zone, Locale aLocale)
    {
        fields = new int[FIELD_COUNT];
        isSet = new boolean[FIELD_COUNT];
        stamp = new int[FIELD_COUNT];
        this.zone = zone;
        setWeekCountData(aLocale);
    }
    public static Calendar getInstance()
    {
        Calendar cal = createCalendar(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        cal.sharedZone = true;
        return cal;
    }
    public static Calendar getInstance(TimeZone zone)
    {
        return createCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }
    public static Calendar getInstance(Locale aLocale)
    {
        Calendar cal = createCalendar(TimeZone.getDefaultRef(), aLocale);
        cal.sharedZone = true;
        return cal;
    }
    public static Calendar getInstance(TimeZone zone,
                                       Locale aLocale)
    {
        return createCalendar(zone, aLocale);
    }
    private static Calendar createCalendar(TimeZone zone,
                                           Locale aLocale)
    {
        Calendar cal = null;
        String caltype = aLocale.getUnicodeLocaleType("ca");
        if (caltype == null) {
            if ("th".equals(aLocale.getLanguage())
                    && ("TH".equals(aLocale.getCountry()))) {
                cal = new BuddhistCalendar(zone, aLocale);
            } else {
                cal = new GregorianCalendar(zone, aLocale);
            }
        } else if (caltype.equals("japanese")) {
            cal = new JapaneseImperialCalendar(zone, aLocale);
        } else if (caltype.equals("buddhist")) {
            cal = new BuddhistCalendar(zone, aLocale);
        } else {
            cal = new GregorianCalendar(zone, aLocale);
        }
        return cal;
    }
    public static synchronized Locale[] getAvailableLocales()
    {
        return DateFormat.getAvailableLocales();
    }
    protected abstract void computeTime();
    protected abstract void computeFields();
    public final Date getTime() {
        return new Date(getTimeInMillis());
    }
    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }
    public long getTimeInMillis() {
        if (!isTimeSet) {
            updateTime();
        }
        return time;
    }
    public void setTimeInMillis(long millis) {
        if (time == millis && isTimeSet && areFieldsSet && areAllFieldsSet
            && (zone instanceof ZoneInfo) && !((ZoneInfo)zone).isDirty()) {
            return;
        }
        time = millis;
        isTimeSet = true;
        areFieldsSet = false;
        computeFields();
        areAllFieldsSet = areFieldsSet = true;
    }
    public int get(int field)
    {
        complete();
        return internalGet(field);
    }
    protected final int internalGet(int field)
    {
        return fields[field];
    }
    final void internalSet(int field, int value)
    {
        fields[field] = value;
    }
    public void set(int field, int value)
    {
        if (areFieldsSet && !areAllFieldsSet) {
            computeFields();
        }
        internalSet(field, value);
        isTimeSet = false;
        areFieldsSet = false;
        isSet[field] = true;
        stamp[field] = nextStamp++;
        if (nextStamp == Integer.MAX_VALUE) {
            adjustStamp();
        }
    }
    public final void set(int year, int month, int date)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
    }
    public final void set(int year, int month, int date, int hourOfDay, int minute)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
    }
    public final void set(int year, int month, int date, int hourOfDay, int minute,
                          int second)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
    }
    public final void clear()
    {
        for (int i = 0; i < fields.length; ) {
            stamp[i] = fields[i] = 0; 
            isSet[i++] = false;
        }
        areAllFieldsSet = areFieldsSet = false;
        isTimeSet = false;
    }
    public final void clear(int field)
    {
        fields[field] = 0;
        stamp[field] = UNSET;
        isSet[field] = false;
        areAllFieldsSet = areFieldsSet = false;
        isTimeSet = false;
    }
    public final boolean isSet(int field)
    {
        return stamp[field] != UNSET;
    }
    public String getDisplayName(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, ALL_STYLES, LONG, locale,
                                    ERA_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = getFieldStrings(field, style, symbols);
        if (strings != null) {
            int fieldValue = get(field);
            if (fieldValue < strings.length) {
                return strings[fieldValue];
            }
        }
        return null;
    }
    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, ALL_STYLES, LONG, locale,
                                    ERA_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }
        if (style == ALL_STYLES) {
            Map<String,Integer> shortNames = getDisplayNamesImpl(field, SHORT, locale);
            if (field == ERA || field == AM_PM) {
                return shortNames;
            }
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
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = getFieldStrings(field, style, symbols);
        if (strings != null) {
            Map<String,Integer> names = new HashMap<String,Integer>();
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].length() == 0) {
                    continue;
                }
                names.put(strings[i], i);
            }
            return names;
        }
        return null;
    }
    boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle,
                                   Locale locale, int fieldMask) {
        if (field < 0 || field >= fields.length ||
            style < minStyle || style > maxStyle) {
            throw new IllegalArgumentException();
        }
        if (locale == null) {
            throw new NullPointerException();
        }
        return isFieldSet(fieldMask, field);
    }
    private String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
        String[] strings = null;
        switch (field) {
        case ERA:
            strings = symbols.getEras();
            break;
        case MONTH:
            strings = (style == LONG) ? symbols.getMonths() : symbols.getShortMonths();
            break;
        case DAY_OF_WEEK:
            strings = (style == LONG) ? symbols.getWeekdays() : symbols.getShortWeekdays();
            break;
        case AM_PM:
            strings = symbols.getAmPmStrings();
            break;
        }
        return strings;
    }
    protected void complete()
    {
        if (!isTimeSet)
            updateTime();
        if (!areFieldsSet || !areAllFieldsSet) {
            computeFields(); 
            areAllFieldsSet = areFieldsSet = true;
        }
    }
    final boolean isExternallySet(int field) {
        return stamp[field] >= MINIMUM_USER_STAMP;
    }
    final int getSetStateFields() {
        int mask = 0;
        for (int i = 0; i < fields.length; i++) {
            if (stamp[i] != UNSET) {
                mask |= 1 << i;
            }
        }
        return mask;
    }
    final void setFieldsComputed(int fieldMask) {
        if (fieldMask == ALL_FIELDS) {
            for (int i = 0; i < fields.length; i++) {
                stamp[i] = COMPUTED;
                isSet[i] = true;
            }
            areFieldsSet = areAllFieldsSet = true;
        } else {
            for (int i = 0; i < fields.length; i++) {
                if ((fieldMask & 1) == 1) {
                    stamp[i] = COMPUTED;
                    isSet[i] = true;
                } else {
                    if (areAllFieldsSet && !isSet[i]) {
                        areAllFieldsSet = false;
                    }
                }
                fieldMask >>>= 1;
            }
        }
    }
    final void setFieldsNormalized(int fieldMask) {
        if (fieldMask != ALL_FIELDS) {
            for (int i = 0; i < fields.length; i++) {
                if ((fieldMask & 1) == 0) {
                    stamp[i] = fields[i] = 0; 
                    isSet[i] = false;
                }
                fieldMask >>= 1;
            }
        }
        areFieldsSet = true;
        areAllFieldsSet = false;
    }
    final boolean isPartiallyNormalized() {
        return areFieldsSet && !areAllFieldsSet;
    }
    final boolean isFullyNormalized() {
        return areFieldsSet && areAllFieldsSet;
    }
    final void setUnnormalized() {
        areFieldsSet = areAllFieldsSet = false;
    }
    static final boolean isFieldSet(int fieldMask, int field) {
        return (fieldMask & (1 << field)) != 0;
    }
    final int selectFields() {
        int fieldMask = YEAR_MASK;
        if (stamp[ERA] != UNSET) {
            fieldMask |= ERA_MASK;
        }
        int dowStamp = stamp[DAY_OF_WEEK];
        int monthStamp = stamp[MONTH];
        int domStamp = stamp[DAY_OF_MONTH];
        int womStamp = aggregateStamp(stamp[WEEK_OF_MONTH], dowStamp);
        int dowimStamp = aggregateStamp(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
        int doyStamp = stamp[DAY_OF_YEAR];
        int woyStamp = aggregateStamp(stamp[WEEK_OF_YEAR], dowStamp);
        int bestStamp = domStamp;
        if (womStamp > bestStamp) {
            bestStamp = womStamp;
        }
        if (dowimStamp > bestStamp) {
            bestStamp = dowimStamp;
        }
        if (doyStamp > bestStamp) {
            bestStamp = doyStamp;
        }
        if (woyStamp > bestStamp) {
            bestStamp = woyStamp;
        }
        if (bestStamp == UNSET) {
            womStamp = stamp[WEEK_OF_MONTH];
            dowimStamp = Math.max(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
            woyStamp = stamp[WEEK_OF_YEAR];
            bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);
            if (bestStamp == UNSET) {
                bestStamp = domStamp = monthStamp;
            }
        }
        if (bestStamp == domStamp ||
           (bestStamp == womStamp && stamp[WEEK_OF_MONTH] >= stamp[WEEK_OF_YEAR]) ||
           (bestStamp == dowimStamp && stamp[DAY_OF_WEEK_IN_MONTH] >= stamp[WEEK_OF_YEAR])) {
            fieldMask |= MONTH_MASK;
            if (bestStamp == domStamp) {
                fieldMask |= DAY_OF_MONTH_MASK;
            } else {
                assert (bestStamp == womStamp || bestStamp == dowimStamp);
                if (dowStamp != UNSET) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                if (womStamp == dowimStamp) {
                    if (stamp[WEEK_OF_MONTH] >= stamp[DAY_OF_WEEK_IN_MONTH]) {
                        fieldMask |= WEEK_OF_MONTH_MASK;
                    } else {
                        fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                    }
                } else {
                    if (bestStamp == womStamp) {
                        fieldMask |= WEEK_OF_MONTH_MASK;
                    } else {
                        assert (bestStamp == dowimStamp);
                        if (stamp[DAY_OF_WEEK_IN_MONTH] != UNSET) {
                            fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                        }
                    }
                }
            }
        } else {
            assert (bestStamp == doyStamp || bestStamp == woyStamp ||
                    bestStamp == UNSET);
            if (bestStamp == doyStamp) {
                fieldMask |= DAY_OF_YEAR_MASK;
            } else {
                assert (bestStamp == woyStamp);
                if (dowStamp != UNSET) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                fieldMask |= WEEK_OF_YEAR_MASK;
            }
        }
        int hourOfDayStamp = stamp[HOUR_OF_DAY];
        int hourStamp = aggregateStamp(stamp[HOUR], stamp[AM_PM]);
        bestStamp = (hourStamp > hourOfDayStamp) ? hourStamp : hourOfDayStamp;
        if (bestStamp == UNSET) {
            bestStamp = Math.max(stamp[HOUR], stamp[AM_PM]);
        }
        if (bestStamp != UNSET) {
            if (bestStamp == hourOfDayStamp) {
                fieldMask |= HOUR_OF_DAY_MASK;
            } else {
                fieldMask |= HOUR_MASK;
                if (stamp[AM_PM] != UNSET) {
                    fieldMask |= AM_PM_MASK;
                }
            }
        }
        if (stamp[MINUTE] != UNSET) {
            fieldMask |= MINUTE_MASK;
        }
        if (stamp[SECOND] != UNSET) {
            fieldMask |= SECOND_MASK;
        }
        if (stamp[MILLISECOND] != UNSET) {
            fieldMask |= MILLISECOND_MASK;
        }
        if (stamp[ZONE_OFFSET] >= MINIMUM_USER_STAMP) {
                fieldMask |= ZONE_OFFSET_MASK;
        }
        if (stamp[DST_OFFSET] >= MINIMUM_USER_STAMP) {
            fieldMask |= DST_OFFSET_MASK;
        }
        return fieldMask;
    }
    private static final int aggregateStamp(int stamp_a, int stamp_b) {
        if (stamp_a == UNSET || stamp_b == UNSET) {
            return UNSET;
        }
        return (stamp_a > stamp_b) ? stamp_a : stamp_b;
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        try {
            Calendar that = (Calendar)obj;
            return compareTo(getMillisOf(that)) == 0 &&
                lenient == that.lenient &&
                firstDayOfWeek == that.firstDayOfWeek &&
                minimalDaysInFirstWeek == that.minimalDaysInFirstWeek &&
                zone.equals(that.zone);
        } catch (Exception e) {
        }
        return false;
    }
    public int hashCode() {
        int otheritems = (lenient ? 1 : 0)
            | (firstDayOfWeek << 1)
            | (minimalDaysInFirstWeek << 4)
            | (zone.hashCode() << 7);
        long t = getMillisOf(this);
        return (int) t ^ (int)(t >> 32) ^ otheritems;
    }
    public boolean before(Object when) {
        return when instanceof Calendar
            && compareTo((Calendar)when) < 0;
    }
    public boolean after(Object when) {
        return when instanceof Calendar
            && compareTo((Calendar)when) > 0;
    }
    public int compareTo(Calendar anotherCalendar) {
        return compareTo(getMillisOf(anotherCalendar));
    }
    abstract public void add(int field, int amount);
    abstract public void roll(int field, boolean up);
    public void roll(int field, int amount)
    {
        while (amount > 0) {
            roll(field, true);
            amount--;
        }
        while (amount < 0) {
            roll(field, false);
            amount++;
        }
    }
    public void setTimeZone(TimeZone value)
    {
        zone = value;
        sharedZone = false;
        areAllFieldsSet = areFieldsSet = false;
    }
    public TimeZone getTimeZone()
    {
        if (sharedZone) {
            zone = (TimeZone) zone.clone();
            sharedZone = false;
        }
        return zone;
    }
    TimeZone getZone() {
        return zone;
    }
    void setZoneShared(boolean shared) {
        sharedZone = shared;
    }
    public void setLenient(boolean lenient)
    {
        this.lenient = lenient;
    }
    public boolean isLenient()
    {
        return lenient;
    }
    public void setFirstDayOfWeek(int value)
    {
        if (firstDayOfWeek == value) {
            return;
        }
        firstDayOfWeek = value;
        invalidateWeekFields();
    }
    public int getFirstDayOfWeek()
    {
        return firstDayOfWeek;
    }
    public void setMinimalDaysInFirstWeek(int value)
    {
        if (minimalDaysInFirstWeek == value) {
            return;
        }
        minimalDaysInFirstWeek = value;
        invalidateWeekFields();
    }
    public int getMinimalDaysInFirstWeek()
    {
        return minimalDaysInFirstWeek;
    }
    public boolean isWeekDateSupported() {
        return false;
    }
    public int getWeekYear() {
        throw new UnsupportedOperationException();
    }
    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        throw new UnsupportedOperationException();
    }
    public int getWeeksInWeekYear() {
        throw new UnsupportedOperationException();
    }
    abstract public int getMinimum(int field);
    abstract public int getMaximum(int field);
    abstract public int getGreatestMinimum(int field);
    abstract public int getLeastMaximum(int field);
    public int getActualMinimum(int field) {
        int fieldValue = getGreatestMinimum(field);
        int endValue = getMinimum(field);
        if (fieldValue == endValue) {
            return fieldValue;
        }
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);
        int result = fieldValue;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            } else {
                result = fieldValue;
                fieldValue--;
            }
        } while (fieldValue >= endValue);
        return result;
    }
    public int getActualMaximum(int field) {
        int fieldValue = getLeastMaximum(field);
        int endValue = getMaximum(field);
        if (fieldValue == endValue) {
            return fieldValue;
        }
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);
        if (field == WEEK_OF_YEAR || field == WEEK_OF_MONTH)
            work.set(DAY_OF_WEEK, firstDayOfWeek);
        int result = fieldValue;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            } else {
                result = fieldValue;
                fieldValue++;
            }
        } while (fieldValue <= endValue);
        return result;
    }
    public Object clone()
    {
        try {
            Calendar other = (Calendar) super.clone();
            other.fields = new int[FIELD_COUNT];
            other.isSet = new boolean[FIELD_COUNT];
            other.stamp = new int[FIELD_COUNT];
            for (int i = 0; i < FIELD_COUNT; i++) {
                other.fields[i] = fields[i];
                other.stamp[i] = stamp[i];
                other.isSet[i] = isSet[i];
            }
            other.zone = (TimeZone) zone.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    private static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH",
        "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR",
        "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET"
    };
    static final String getFieldName(int field) {
        return FIELD_NAME[field];
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder(800);
        buffer.append(getClass().getName()).append('[');
        appendValue(buffer, "time", isTimeSet, time);
        buffer.append(",areFieldsSet=").append(areFieldsSet);
        buffer.append(",areAllFieldsSet=").append(areAllFieldsSet);
        buffer.append(",lenient=").append(lenient);
        buffer.append(",zone=").append(zone);
        appendValue(buffer, ",firstDayOfWeek", true, (long) firstDayOfWeek);
        appendValue(buffer, ",minimalDaysInFirstWeek", true, (long) minimalDaysInFirstWeek);
        for (int i = 0; i < FIELD_COUNT; ++i) {
            buffer.append(',');
            appendValue(buffer, FIELD_NAME[i], isSet(i), (long) fields[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }
    private static final void appendValue(StringBuilder sb, String item, boolean valid, long value) {
        sb.append(item).append('=');
        if (valid) {
            sb.append(value);
        } else {
            sb.append('?');
        }
    }
    private void setWeekCountData(Locale desiredLocale)
    {
        int[] data = cachedLocaleData.get(desiredLocale);
        if (data == null) {  
            ResourceBundle bundle = LocaleData.getCalendarData(desiredLocale);
            data = new int[2];
            data[0] = Integer.parseInt(bundle.getString("firstDayOfWeek"));
            data[1] = Integer.parseInt(bundle.getString("minimalDaysInFirstWeek"));
            cachedLocaleData.putIfAbsent(desiredLocale, data);
        }
        firstDayOfWeek = data[0];
        minimalDaysInFirstWeek = data[1];
    }
    private void updateTime() {
        computeTime();
        isTimeSet = true;
    }
    private int compareTo(long t) {
        long thisTime = getMillisOf(this);
        return (thisTime > t) ? 1 : (thisTime == t) ? 0 : -1;
    }
    private static final long getMillisOf(Calendar calendar) {
        if (calendar.isTimeSet) {
            return calendar.time;
        }
        Calendar cal = (Calendar) calendar.clone();
        cal.setLenient(true);
        return cal.getTimeInMillis();
    }
    private final void adjustStamp() {
        int max = MINIMUM_USER_STAMP;
        int newStamp = MINIMUM_USER_STAMP;
        for (;;) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < stamp.length; i++) {
                int v = stamp[i];
                if (v >= newStamp && min > v) {
                    min = v;
                }
                if (max < v) {
                    max = v;
                }
            }
            if (max != min && min == Integer.MAX_VALUE) {
                break;
            }
            for (int i = 0; i < stamp.length; i++) {
                if (stamp[i] == min) {
                    stamp[i] = newStamp;
                }
            }
            newStamp++;
            if (min == max) {
                break;
            }
        }
        nextStamp = newStamp;
    }
    private void invalidateWeekFields()
    {
        if (stamp[WEEK_OF_MONTH] != COMPUTED &&
            stamp[WEEK_OF_YEAR] != COMPUTED) {
            return;
        }
        Calendar cal = (Calendar) clone();
        cal.setLenient(true);
        cal.clear(WEEK_OF_MONTH);
        cal.clear(WEEK_OF_YEAR);
        if (stamp[WEEK_OF_MONTH] == COMPUTED) {
            int weekOfMonth = cal.get(WEEK_OF_MONTH);
            if (fields[WEEK_OF_MONTH] != weekOfMonth) {
                fields[WEEK_OF_MONTH] = weekOfMonth;
            }
        }
        if (stamp[WEEK_OF_YEAR] == COMPUTED) {
            int weekOfYear = cal.get(WEEK_OF_YEAR);
            if (fields[WEEK_OF_YEAR] != weekOfYear) {
                fields[WEEK_OF_YEAR] = weekOfYear;
            }
        }
    }
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        if (!isTimeSet) {
            try {
                updateTime();
            }
            catch (IllegalArgumentException e) {}
        }
        TimeZone savedZone = null;
        if (zone instanceof ZoneInfo) {
            SimpleTimeZone stz = ((ZoneInfo)zone).getLastRuleInstance();
            if (stz == null) {
                stz = new SimpleTimeZone(zone.getRawOffset(), zone.getID());
            }
            savedZone = zone;
            zone = stz;
        }
        stream.defaultWriteObject();
        stream.writeObject(savedZone);
        if (savedZone != null) {
            zone = savedZone;
        }
    }
    private static class CalendarAccessControlContext {
        private static final AccessControlContext INSTANCE;
        static {
            RuntimePermission perm = new RuntimePermission("accessClassInPackage.sun.util.calendar");
            PermissionCollection perms = perm.newPermissionCollection();
            perms.add(perm);
            INSTANCE = new AccessControlContext(new ProtectionDomain[] {
                                                    new ProtectionDomain(null, perms)
                                                });
        }
    }
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        final ObjectInputStream input = stream;
        input.defaultReadObject();
        stamp = new int[FIELD_COUNT];
        if (serialVersionOnStream >= 2)
        {
            isTimeSet = true;
            if (fields == null) fields = new int[FIELD_COUNT];
            if (isSet == null) isSet = new boolean[FIELD_COUNT];
        }
        else if (serialVersionOnStream >= 0)
        {
            for (int i=0; i<FIELD_COUNT; ++i)
                stamp[i] = isSet[i] ? COMPUTED : UNSET;
        }
        serialVersionOnStream = currentSerialVersion;
        ZoneInfo zi = null;
        try {
            zi = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ZoneInfo>() {
                        public ZoneInfo run() throws Exception {
                            return (ZoneInfo) input.readObject();
                        }
                    },
                    CalendarAccessControlContext.INSTANCE);
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (!(e instanceof OptionalDataException)) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) e;
                }
                throw new RuntimeException(e);
            }
        }
        if (zi != null) {
            zone = zi;
        }
        if (zone instanceof SimpleTimeZone) {
            String id = zone.getID();
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz != null && tz.hasSameRules(zone) && tz.getID().equals(id)) {
                zone = tz;
            }
        }
    }
}
