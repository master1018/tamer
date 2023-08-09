public class SimpleDateFormat extends DateFormat {
    static final long serialVersionUID = 4774881970558875024L;
    static final int currentSerialVersion = 1;
    private int serialVersionOnStream = currentSerialVersion;
    private String pattern;
    transient private NumberFormat originalNumberFormat;
    transient private String originalNumberPattern;
    transient private char minusSign = '-';
    transient private boolean hasFollowingMinusSign = false;
    transient private char[] compiledPattern;
    private final static int TAG_QUOTE_ASCII_CHAR       = 100;
    private final static int TAG_QUOTE_CHARS            = 101;
    transient private char zeroDigit;
    private DateFormatSymbols formatData;
    private Date defaultCenturyStart;
    transient private int defaultCenturyStartYear;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final String GMT = "GMT";
    private static final ConcurrentMap<Locale, String[]> cachedLocaleData
        = new ConcurrentHashMap<Locale, String[]>(3);
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData
        = new ConcurrentHashMap<Locale, NumberFormat>(3);
    private Locale locale;
    transient boolean useDateFormatSymbols;
    public SimpleDateFormat() {
        this(SHORT, SHORT, Locale.getDefault(Locale.Category.FORMAT));
    }
    public SimpleDateFormat(String pattern)
    {
        this(pattern, Locale.getDefault(Locale.Category.FORMAT));
    }
    public SimpleDateFormat(String pattern, Locale locale)
    {
        if (pattern == null || locale == null) {
            throw new NullPointerException();
        }
        initializeCalendar(locale);
        this.pattern = pattern;
        this.formatData = DateFormatSymbols.getInstanceRef(locale);
        this.locale = locale;
        initialize(locale);
    }
    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols)
    {
        if (pattern == null || formatSymbols == null) {
            throw new NullPointerException();
        }
        this.pattern = pattern;
        this.formatData = (DateFormatSymbols) formatSymbols.clone();
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        initializeCalendar(this.locale);
        initialize(this.locale);
        useDateFormatSymbols = true;
    }
    SimpleDateFormat(int timeStyle, int dateStyle, Locale loc) {
        if (loc == null) {
            throw new NullPointerException();
        }
        this.locale = loc;
        initializeCalendar(loc);
        String[] dateTimePatterns = cachedLocaleData.get(loc);
        if (dateTimePatterns == null) { 
            ResourceBundle r = LocaleData.getDateFormatData(loc);
            if (!isGregorianCalendar()) {
                try {
                    dateTimePatterns = r.getStringArray(getCalendarName() + ".DateTimePatterns");
                } catch (MissingResourceException e) {
                }
            }
            if (dateTimePatterns == null) {
                dateTimePatterns = r.getStringArray("DateTimePatterns");
            }
            cachedLocaleData.putIfAbsent(loc, dateTimePatterns);
        }
        formatData = DateFormatSymbols.getInstanceRef(loc);
        if ((timeStyle >= 0) && (dateStyle >= 0)) {
            Object[] dateTimeArgs = {dateTimePatterns[timeStyle],
                                     dateTimePatterns[dateStyle + 4]};
            pattern = MessageFormat.format(dateTimePatterns[8], dateTimeArgs);
        }
        else if (timeStyle >= 0) {
            pattern = dateTimePatterns[timeStyle];
        }
        else if (dateStyle >= 0) {
            pattern = dateTimePatterns[dateStyle + 4];
        }
        else {
            throw new IllegalArgumentException("No date or time style specified");
        }
        initialize(loc);
    }
    private void initialize(Locale loc) {
        compiledPattern = compile(pattern);
        numberFormat = cachedNumberFormatData.get(loc);
        if (numberFormat == null) { 
            numberFormat = NumberFormat.getIntegerInstance(loc);
            numberFormat.setGroupingUsed(false);
            cachedNumberFormatData.putIfAbsent(loc, numberFormat);
        }
        numberFormat = (NumberFormat) numberFormat.clone();
        initializeDefaultCentury();
    }
    private void initializeCalendar(Locale loc) {
        if (calendar == null) {
            assert loc != null;
            calendar = Calendar.getInstance(TimeZone.getDefault(), loc);
        }
    }
    private char[] compile(String pattern) {
        int length = pattern.length();
        boolean inQuote = false;
        StringBuilder compiledPattern = new StringBuilder(length * 2);
        StringBuilder tmpBuffer = null;
        int count = 0;
        int lastTag = -1;
        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                if ((i + 1) < length) {
                    c = pattern.charAt(i + 1);
                    if (c == '\'') {
                        i++;
                        if (count != 0) {
                            encode(lastTag, count, compiledPattern);
                            lastTag = -1;
                            count = 0;
                        }
                        if (inQuote) {
                            tmpBuffer.append(c);
                        } else {
                            compiledPattern.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                        }
                        continue;
                    }
                }
                if (!inQuote) {
                    if (count != 0) {
                        encode(lastTag, count, compiledPattern);
                        lastTag = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(0);
                    }
                    inQuote = true;
                } else {
                    int len = tmpBuffer.length();
                    if (len == 1) {
                        char ch = tmpBuffer.charAt(0);
                        if (ch < 128) {
                            compiledPattern.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | ch));
                        } else {
                            compiledPattern.append((char)(TAG_QUOTE_CHARS << 8 | 1));
                            compiledPattern.append(ch);
                        }
                    } else {
                        encode(TAG_QUOTE_CHARS, len, compiledPattern);
                        compiledPattern.append(tmpBuffer);
                    }
                    inQuote = false;
                }
                continue;
            }
            if (inQuote) {
                tmpBuffer.append(c);
                continue;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                if (count != 0) {
                    encode(lastTag, count, compiledPattern);
                    lastTag = -1;
                    count = 0;
                }
                if (c < 128) {
                    compiledPattern.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                } else {
                    int j;
                    for (j = i + 1; j < length; j++) {
                        char d = pattern.charAt(j);
                        if (d == '\'' || (d >= 'a' && d <= 'z' || d >= 'A' && d <= 'Z')) {
                            break;
                        }
                    }
                    compiledPattern.append((char)(TAG_QUOTE_CHARS << 8 | (j - i)));
                    for (; i < j; i++) {
                        compiledPattern.append(pattern.charAt(i));
                    }
                    i--;
                }
                continue;
            }
            int tag;
            if ((tag = DateFormatSymbols.patternChars.indexOf(c)) == -1) {
                throw new IllegalArgumentException("Illegal pattern character " +
                                                   "'" + c + "'");
            }
            if (lastTag == -1 || lastTag == tag) {
                lastTag = tag;
                count++;
                continue;
            }
            encode(lastTag, count, compiledPattern);
            lastTag = tag;
            count = 1;
        }
        if (inQuote) {
            throw new IllegalArgumentException("Unterminated quote");
        }
        if (count != 0) {
            encode(lastTag, count, compiledPattern);
        }
        int len = compiledPattern.length();
        char[] r = new char[len];
        compiledPattern.getChars(0, len, r, 0);
        return r;
    }
    private static final void encode(int tag, int length, StringBuilder buffer) {
        if (tag == PATTERN_ISO_ZONE && length >= 4) {
            throw new IllegalArgumentException("invalid ISO 8601 format: length=" + length);
        }
        if (length < 255) {
            buffer.append((char)(tag << 8 | length));
        } else {
            buffer.append((char)((tag << 8) | 0xff));
            buffer.append((char)(length >>> 16));
            buffer.append((char)(length & 0xffff));
        }
    }
    private void initializeDefaultCentury() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add( Calendar.YEAR, -80 );
        parseAmbiguousDatesAsAfter(calendar.getTime());
    }
    private void parseAmbiguousDatesAsAfter(Date startDate) {
        defaultCenturyStart = startDate;
        calendar.setTime(startDate);
        defaultCenturyStartYear = calendar.get(Calendar.YEAR);
    }
    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(new Date(startDate.getTime()));
    }
    public Date get2DigitYearStart() {
        return (Date) defaultCenturyStart.clone();
    }
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                               FieldPosition pos)
    {
        pos.beginIndex = pos.endIndex = 0;
        return format(date, toAppendTo, pos.getFieldDelegate());
    }
    private StringBuffer format(Date date, StringBuffer toAppendTo,
                                FieldDelegate delegate) {
        calendar.setTime(date);
        boolean useDateFormatSymbols = useDateFormatSymbols();
        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }
            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                toAppendTo.append((char)count);
                break;
            case TAG_QUOTE_CHARS:
                toAppendTo.append(compiledPattern, i, count);
                i += count;
                break;
            default:
                subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
                break;
            }
        }
        return toAppendTo;
    }
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        CharacterIteratorFieldDelegate delegate = new
                         CharacterIteratorFieldDelegate();
        if (obj instanceof Date) {
            format((Date)obj, sb, delegate);
        }
        else if (obj instanceof Number) {
            format(new Date(((Number)obj).longValue()), sb, delegate);
        }
        else if (obj == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator must be passed non-null object");
        }
        else {
            throw new IllegalArgumentException(
                             "Cannot format given Object as a Date");
        }
        return delegate.getIterator(sb.toString());
    }
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD =
    {
        Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.DATE,
        Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
        Calendar.SECOND, Calendar.MILLISECOND, Calendar.DAY_OF_WEEK,
        Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK_IN_MONTH,
        Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH,
        Calendar.AM_PM, Calendar.HOUR, Calendar.HOUR, Calendar.ZONE_OFFSET,
        Calendar.ZONE_OFFSET,
        CalendarBuilder.WEEK_YEAR,
        CalendarBuilder.ISO_DAY_OF_WEEK,
        Calendar.ZONE_OFFSET
    };
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = {
        DateFormat.ERA_FIELD, DateFormat.YEAR_FIELD, DateFormat.MONTH_FIELD,
        DateFormat.DATE_FIELD, DateFormat.HOUR_OF_DAY1_FIELD,
        DateFormat.HOUR_OF_DAY0_FIELD, DateFormat.MINUTE_FIELD,
        DateFormat.SECOND_FIELD, DateFormat.MILLISECOND_FIELD,
        DateFormat.DAY_OF_WEEK_FIELD, DateFormat.DAY_OF_YEAR_FIELD,
        DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD, DateFormat.WEEK_OF_YEAR_FIELD,
        DateFormat.WEEK_OF_MONTH_FIELD, DateFormat.AM_PM_FIELD,
        DateFormat.HOUR1_FIELD, DateFormat.HOUR0_FIELD,
        DateFormat.TIMEZONE_FIELD, DateFormat.TIMEZONE_FIELD,
        DateFormat.YEAR_FIELD, DateFormat.DAY_OF_WEEK_FIELD,
        DateFormat.TIMEZONE_FIELD
    };
    private static final Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = {
        Field.ERA, Field.YEAR, Field.MONTH, Field.DAY_OF_MONTH,
        Field.HOUR_OF_DAY1, Field.HOUR_OF_DAY0, Field.MINUTE,
        Field.SECOND, Field.MILLISECOND, Field.DAY_OF_WEEK,
        Field.DAY_OF_YEAR, Field.DAY_OF_WEEK_IN_MONTH,
        Field.WEEK_OF_YEAR, Field.WEEK_OF_MONTH,
        Field.AM_PM, Field.HOUR1, Field.HOUR0, Field.TIME_ZONE,
        Field.TIME_ZONE,
        Field.YEAR, Field.DAY_OF_WEEK,
        Field.TIME_ZONE
    };
    private void subFormat(int patternCharIndex, int count,
                           FieldDelegate delegate, StringBuffer buffer,
                           boolean useDateFormatSymbols)
    {
        int     maxIntCount = Integer.MAX_VALUE;
        String  current = null;
        int     beginOffset = buffer.length();
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        int value;
        if (field == CalendarBuilder.WEEK_YEAR) {
            if (calendar.isWeekDateSupported()) {
                value = calendar.getWeekYear();
            } else {
                patternCharIndex = PATTERN_YEAR;
                field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
                value = calendar.get(field);
            }
        } else if (field == CalendarBuilder.ISO_DAY_OF_WEEK) {
            value = CalendarBuilder.toISODayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            value = calendar.get(field);
        }
        int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
        if (!useDateFormatSymbols && field != CalendarBuilder.ISO_DAY_OF_WEEK) {
            current = calendar.getDisplayName(field, style, locale);
        }
        switch (patternCharIndex) {
        case PATTERN_ERA: 
            if (useDateFormatSymbols) {
                String[] eras = formatData.getEras();
                if (value < eras.length)
                    current = eras[value];
            }
            if (current == null)
                current = "";
            break;
        case PATTERN_WEEK_YEAR: 
        case PATTERN_YEAR:      
            if (calendar instanceof GregorianCalendar) {
                if (count != 2)
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                else 
                    zeroPaddingNumber(value, 2, 2, buffer); 
            } else {
                if (current == null) {
                    zeroPaddingNumber(value, style == Calendar.LONG ? 1 : count,
                                      maxIntCount, buffer);
                }
            }
            break;
        case PATTERN_MONTH: 
            if (useDateFormatSymbols) {
                String[] months;
                if (count >= 4) {
                    months = formatData.getMonths();
                    current = months[value];
                } else if (count == 3) {
                    months = formatData.getShortMonths();
                    current = months[value];
                }
            } else {
                if (count < 3) {
                    current = null;
                }
            }
            if (current == null) {
                zeroPaddingNumber(value+1, count, maxIntCount, buffer);
            }
            break;
        case PATTERN_HOUR_OF_DAY1: 
            if (current == null) {
                if (value == 0)
                    zeroPaddingNumber(calendar.getMaximum(Calendar.HOUR_OF_DAY)+1,
                                      count, maxIntCount, buffer);
                else
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
            break;
        case PATTERN_DAY_OF_WEEK: 
            if (useDateFormatSymbols) {
                String[] weekdays;
                if (count >= 4) {
                    weekdays = formatData.getWeekdays();
                    current = weekdays[value];
                } else { 
                    weekdays = formatData.getShortWeekdays();
                    current = weekdays[value];
                }
            }
            break;
        case PATTERN_AM_PM:    
            if (useDateFormatSymbols) {
                String[] ampm = formatData.getAmPmStrings();
                current = ampm[value];
            }
            break;
        case PATTERN_HOUR1:    
            if (current == null) {
                if (value == 0)
                    zeroPaddingNumber(calendar.getLeastMaximum(Calendar.HOUR)+1,
                                      count, maxIntCount, buffer);
                else
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
            break;
        case PATTERN_ZONE_NAME: 
            if (current == null) {
                if (formatData.locale == null || formatData.isZoneStringsSet) {
                    int zoneIndex =
                        formatData.getZoneIndex(calendar.getTimeZone().getID());
                    if (zoneIndex == -1) {
                        value = calendar.get(Calendar.ZONE_OFFSET) +
                            calendar.get(Calendar.DST_OFFSET);
                        buffer.append(ZoneInfoFile.toCustomID(value));
                    } else {
                        int index = (calendar.get(Calendar.DST_OFFSET) == 0) ? 1: 3;
                        if (count < 4) {
                            index++;
                        }
                        String[][] zoneStrings = formatData.getZoneStringsWrapper();
                        buffer.append(zoneStrings[zoneIndex][index]);
                    }
                } else {
                    TimeZone tz = calendar.getTimeZone();
                    boolean daylight = (calendar.get(Calendar.DST_OFFSET) != 0);
                    int tzstyle = (count < 4 ? TimeZone.SHORT : TimeZone.LONG);
                    buffer.append(tz.getDisplayName(daylight, tzstyle, formatData.locale));
                }
            }
            break;
        case PATTERN_ZONE_VALUE: 
            value = (calendar.get(Calendar.ZONE_OFFSET) +
                     calendar.get(Calendar.DST_OFFSET)) / 60000;
            int width = 4;
            if (value >= 0) {
                buffer.append('+');
            } else {
                width++;
            }
            int num = (value / 60) * 100 + (value % 60);
            CalendarUtils.sprintf0d(buffer, num, width);
            break;
        case PATTERN_ISO_ZONE:   
            value = calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET);
            if (value == 0) {
                buffer.append('Z');
                break;
            }
            value /=  60000;
            if (value >= 0) {
                buffer.append('+');
            } else {
                buffer.append('-');
                value = -value;
            }
            CalendarUtils.sprintf0d(buffer, value / 60, 2);
            if (count == 1) {
                break;
            }
            if (count == 3) {
                buffer.append(':');
            }
            CalendarUtils.sprintf0d(buffer, value % 60, 2);
            break;
        default:
            if (current == null) {
                zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
            break;
        } 
        if (current != null) {
            buffer.append(current);
        }
        int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex];
        Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex];
        delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), buffer);
    }
    private final void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer)
    {
        try {
            if (zeroDigit == 0) {
                zeroDigit = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < 100 && minDigits >= 1 && minDigits <= 2) {
                    if (value < 10) {
                        if (minDigits == 2) {
                            buffer.append(zeroDigit);
                        }
                        buffer.append((char)(zeroDigit + value));
                    } else {
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                    }
                    return;
                } else if (value >= 1000 && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char)(zeroDigit + value / 1000));
                        value %= 1000;
                        buffer.append((char)(zeroDigit + value / 100));
                        value %= 100;
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                        return;
                    }
                    if (minDigits == 2 && maxDigits == 2) {
                        zeroPaddingNumber(value % 100, 2, 2, buffer);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }
        numberFormat.setMinimumIntegerDigits(minDigits);
        numberFormat.setMaximumIntegerDigits(maxDigits);
        numberFormat.format((long)value, buffer, DontCareFieldPosition.INSTANCE);
    }
    public Date parse(String text, ParsePosition pos)
    {
        checkNegativeNumberExpression();
        int start = pos.index;
        int oldStart = start;
        int textLength = text.length();
        boolean[] ambiguousYear = {false};
        CalendarBuilder calb = new CalendarBuilder();
        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }
            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                if (start >= textLength || text.charAt(start) != (char)count) {
                    pos.index = oldStart;
                    pos.errorIndex = start;
                    return null;
                }
                start++;
                break;
            case TAG_QUOTE_CHARS:
                while (count-- > 0) {
                    if (start >= textLength || text.charAt(start) != compiledPattern[i++]) {
                        pos.index = oldStart;
                        pos.errorIndex = start;
                        return null;
                    }
                    start++;
                }
                break;
            default:
                boolean obeyCount = false;
                boolean useFollowingMinusSignAsDelimiter = false;
                if (i < compiledPattern.length) {
                    int nextTag = compiledPattern[i] >>> 8;
                    if (!(nextTag == TAG_QUOTE_ASCII_CHAR ||
                          nextTag == TAG_QUOTE_CHARS)) {
                        obeyCount = true;
                    }
                    if (hasFollowingMinusSign &&
                        (nextTag == TAG_QUOTE_ASCII_CHAR ||
                         nextTag == TAG_QUOTE_CHARS)) {
                        int c;
                        if (nextTag == TAG_QUOTE_ASCII_CHAR) {
                            c = compiledPattern[i] & 0xff;
                        } else {
                            c = compiledPattern[i+1];
                        }
                        if (c == minusSign) {
                            useFollowingMinusSignAsDelimiter = true;
                        }
                    }
                }
                start = subParse(text, start, tag, count, obeyCount,
                                 ambiguousYear, pos,
                                 useFollowingMinusSignAsDelimiter, calb);
                if (start < 0) {
                    pos.index = oldStart;
                    return null;
                }
            }
        }
        pos.index = start;
        Date parsedDate;
        try {
            parsedDate = calb.establish(calendar).getTime();
            if (ambiguousYear[0]) {
                if (parsedDate.before(defaultCenturyStart)) {
                    parsedDate = calb.addYear(100).establish(calendar).getTime();
                }
            }
        }
        catch (IllegalArgumentException e) {
            pos.errorIndex = start;
            pos.index = oldStart;
            return null;
        }
        return parsedDate;
    }
    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb)
    {
        int i = 0;
        int count = data.length;
        if (field == Calendar.DAY_OF_WEEK) i = 1;
        int bestMatchLength = 0, bestMatch = -1;
        for (; i<count; ++i)
        {
            int length = data[i].length();
            if (length > bestMatchLength &&
                text.regionMatches(true, start, data[i], 0, length))
            {
                bestMatch = i;
                bestMatchLength = length;
            }
        }
        if (bestMatch >= 0)
        {
            calb.set(field, bestMatch);
            return start + bestMatchLength;
        }
        return -start;
    }
    private int matchString(String text, int start, int field,
                            Map<String,Integer> data, CalendarBuilder calb) {
        if (data != null) {
            String bestMatch = null;
            for (String name : data.keySet()) {
                int length = name.length();
                if (bestMatch == null || length > bestMatch.length()) {
                    if (text.regionMatches(true, start, name, 0, length)) {
                        bestMatch = name;
                    }
                }
            }
            if (bestMatch != null) {
                calb.set(field, data.get(bestMatch));
                return start + bestMatch.length();
            }
        }
        return -start;
    }
    private int matchZoneString(String text, int start, String[] zoneNames) {
        for (int i = 1; i <= 4; ++i) {
            String zoneName = zoneNames[i];
            if (text.regionMatches(true, start,
                                   zoneName, 0, zoneName.length())) {
                return i;
            }
        }
        return -1;
    }
    private boolean matchDSTString(String text, int start, int zoneIndex, int standardIndex,
                                   String[][] zoneStrings) {
        int index = standardIndex + 2;
        String zoneName  = zoneStrings[zoneIndex][index];
        if (text.regionMatches(true, start,
                               zoneName, 0, zoneName.length())) {
            return true;
        }
        return false;
    }
    private int subParseZoneString(String text, int start, CalendarBuilder calb) {
        boolean useSameName = false; 
        TimeZone currentTimeZone = getTimeZone();
        int zoneIndex = formatData.getZoneIndex(currentTimeZone.getID());
        TimeZone tz = null;
        String[][] zoneStrings = formatData.getZoneStringsWrapper();
        String[] zoneNames = null;
        int nameIndex = 0;
        if (zoneIndex != -1) {
            zoneNames = zoneStrings[zoneIndex];
            if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                if (nameIndex <= 2) {
                    useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                }
                tz = TimeZone.getTimeZone(zoneNames[0]);
            }
        }
        if (tz == null) {
            zoneIndex = formatData.getZoneIndex(TimeZone.getDefault().getID());
            if (zoneIndex != -1) {
                zoneNames = zoneStrings[zoneIndex];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                }
            }
        }
        if (tz == null) {
            int len = zoneStrings.length;
            for (int i = 0; i < len; i++) {
                zoneNames = zoneStrings[i];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                    break;
                }
            }
        }
        if (tz != null) { 
            if (!tz.equals(currentTimeZone)) {
                setTimeZone(tz);
            }
            int dstAmount = (nameIndex >= 3) ? tz.getDSTSavings() : 0;
            if (!(useSameName || (nameIndex >= 3 && dstAmount == 0))) {
                calb.set(Calendar.ZONE_OFFSET, tz.getRawOffset())
                    .set(Calendar.DST_OFFSET, dstAmount);
            }
            return (start + zoneNames[nameIndex].length());
        }
        return 0;
    }
    private int subParseNumericZone(String text, int start, int sign, int count,
                                    boolean colon, CalendarBuilder calb) {
        int index = start;
      parse:
        try {
            char c = text.charAt(index++);
            int hours;
            if (!isDigit(c)) {
                break parse;
            }
            hours = c - '0';
            c = text.charAt(index++);
            if (isDigit(c)) {
                hours = hours * 10 + (c - '0');
            } else {
                if (count > 0 || !colon) {
                    break parse;
                }
                --index;
            }
            if (hours > 23) {
                break parse;
            }
            int minutes = 0;
            if (count != 1) {
                c = text.charAt(index++);
                if (colon) {
                    if (c != ':') {
                        break parse;
                    }
                    c = text.charAt(index++);
                }
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = c - '0';
                c = text.charAt(index++);
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = minutes * 10 + (c - '0');
                if (minutes > 59) {
                    break parse;
                }
            }
            minutes += hours * 60;
            calb.set(Calendar.ZONE_OFFSET, minutes * MILLIS_PER_MINUTE * sign)
                .set(Calendar.DST_OFFSET, 0);
            return index;
        } catch (IndexOutOfBoundsException e) {
        }
        return  1 - index; 
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private int subParse(String text, int start, int patternCharIndex, int count,
                         boolean obeyCount, boolean[] ambiguousYear,
                         ParsePosition origPos,
                         boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
        Number number = null;
        int value = 0;
        ParsePosition pos = new ParsePosition(0);
        pos.index = start;
        if (patternCharIndex == PATTERN_WEEK_YEAR && !calendar.isWeekDateSupported()) {
            patternCharIndex = PATTERN_YEAR;
        }
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        for (;;) {
            if (pos.index >= text.length()) {
                origPos.errorIndex = start;
                return -1;
            }
            char c = text.charAt(pos.index);
            if (c != ' ' && c != '\t') break;
            ++pos.index;
        }
      parsing:
        {
            if (patternCharIndex == PATTERN_HOUR_OF_DAY1 ||
                patternCharIndex == PATTERN_HOUR1 ||
                (patternCharIndex == PATTERN_MONTH && count <= 2) ||
                patternCharIndex == PATTERN_YEAR ||
                patternCharIndex == PATTERN_WEEK_YEAR) {
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number == null) {
                    if (patternCharIndex != PATTERN_YEAR || calendar instanceof GregorianCalendar) {
                        break parsing;
                    }
                } else {
                    value = number.intValue();
                    if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }
                }
            }
            boolean useDateFormatSymbols = useDateFormatSymbols();
            int index;
            switch (patternCharIndex) {
            case PATTERN_ERA: 
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.ERA, formatData.getEras(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = calendar.getDisplayNames(field,
                                                                        Calendar.ALL_STYLES,
                                                                        locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;
            case PATTERN_WEEK_YEAR: 
            case PATTERN_YEAR:      
                if (!(calendar instanceof GregorianCalendar)) {
                    int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
                    Map<String, Integer> map = calendar.getDisplayNames(field, style, locale);
                    if (map != null) {
                        if ((index = matchString(text, start, field, map, calb)) > 0) {
                            return index;
                        }
                    }
                    calb.set(field, value);
                    return pos.index;
                }
                if (count <= 2 && (pos.index - start) == 2
                    && Character.isDigit(text.charAt(start))
                    && Character.isDigit(text.charAt(start+1))) {
                    int ambiguousTwoDigitYear = defaultCenturyStartYear % 100;
                    ambiguousYear[0] = value == ambiguousTwoDigitYear;
                    value += (defaultCenturyStartYear/100)*100 +
                        (value < ambiguousTwoDigitYear ? 100 : 0);
                }
                calb.set(field, value);
                return pos.index;
            case PATTERN_MONTH: 
                if (count <= 2) 
                {
                    calb.set(Calendar.MONTH, value - 1);
                    return pos.index;
                }
                if (useDateFormatSymbols) {
                    int newStart = 0;
                    if ((newStart = matchString(text, start, Calendar.MONTH,
                                                formatData.getMonths(), calb)) > 0) {
                        return newStart;
                    }
                    if ((index = matchString(text, start, Calendar.MONTH,
                                             formatData.getShortMonths(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = calendar.getDisplayNames(field,
                                                                        Calendar.ALL_STYLES,
                                                                        locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;
            case PATTERN_HOUR_OF_DAY1: 
                if (!isLenient()) {
                    if (value < 1 || value > 24) {
                        break parsing;
                    }
                }
                if (value == calendar.getMaximum(Calendar.HOUR_OF_DAY)+1)
                    value = 0;
                calb.set(Calendar.HOUR_OF_DAY, value);
                return pos.index;
            case PATTERN_DAY_OF_WEEK:  
                {
                    if (useDateFormatSymbols) {
                        int newStart = 0;
                        if ((newStart=matchString(text, start, Calendar.DAY_OF_WEEK,
                                                  formatData.getWeekdays(), calb)) > 0) {
                            return newStart;
                        }
                        if ((index = matchString(text, start, Calendar.DAY_OF_WEEK,
                                                 formatData.getShortWeekdays(), calb)) > 0) {
                            return index;
                        }
                    } else {
                        int[] styles = { Calendar.LONG, Calendar.SHORT };
                        for (int style : styles) {
                            Map<String,Integer> map = calendar.getDisplayNames(field, style, locale);
                            if ((index = matchString(text, start, field, map, calb)) > 0) {
                                return index;
                            }
                        }
                    }
                }
                break parsing;
            case PATTERN_AM_PM:    
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.AM_PM,
                                             formatData.getAmPmStrings(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String,Integer> map = calendar.getDisplayNames(field, Calendar.ALL_STYLES, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;
            case PATTERN_HOUR1: 
                if (!isLenient()) {
                    if (value < 1 || value > 12) {
                        break parsing;
                    }
                }
                if (value == calendar.getLeastMaximum(Calendar.HOUR)+1)
                    value = 0;
                calb.set(Calendar.HOUR, value);
                return pos.index;
            case PATTERN_ZONE_NAME:  
            case PATTERN_ZONE_VALUE: 
                {
                    int sign = 0;
                    try {
                        char c = text.charAt(pos.index);
                        if (c == '+') {
                            sign = 1;
                        } else if (c == '-') {
                            sign = -1;
                        }
                        if (sign == 0) {
                            if ((c == 'G' || c == 'g')
                                && (text.length() - start) >= GMT.length()
                                && text.regionMatches(true, start, GMT, 0, GMT.length())) {
                                pos.index = start + GMT.length();
                                if ((text.length() - pos.index) > 0) {
                                    c = text.charAt(pos.index);
                                    if (c == '+') {
                                        sign = 1;
                                    } else if (c == '-') {
                                        sign = -1;
                                    }
                                }
                                if (sign == 0) {    
                                    calb.set(Calendar.ZONE_OFFSET, 0)
                                        .set(Calendar.DST_OFFSET, 0);
                                    return pos.index;
                                }
                                int i = subParseNumericZone(text, ++pos.index,
                                                            sign, 0, true, calb);
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            } else {
                                int i = subParseZoneString(text, pos.index, calb);
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            }
                        } else {
                            int i = subParseNumericZone(text, ++pos.index,
                                                        sign, 0, false, calb);
                            if (i > 0) {
                                return i;
                            }
                            pos.index = -i;
                        }
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                break parsing;
            case PATTERN_ISO_ZONE:   
                {
                    if ((text.length() - pos.index) <= 0) {
                        break parsing;
                    }
                    int sign = 0;
                    char c = text.charAt(pos.index);
                    if (c == 'Z') {
                        calb.set(Calendar.ZONE_OFFSET, 0).set(Calendar.DST_OFFSET, 0);
                        return ++pos.index;
                    }
                    if (c == '+') {
                        sign = 1;
                    } else if (c == '-') {
                        sign = -1;
                    } else {
                        ++pos.index;
                        break parsing;
                    }
                    int i = subParseNumericZone(text, ++pos.index, sign, count,
                                                count == 3, calb);
                    if (i > 0) {
                        return i;
                    }
                    pos.index = -i;
                }
                break parsing;
            default:
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number != null) {
                    value = number.intValue();
                    if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }
                    calb.set(field, value);
                    return pos.index;
                }
                break parsing;
            }
        }
        origPos.errorIndex = pos.index;
        return -1;
    }
    private final String getCalendarName() {
        return calendar.getClass().getName();
    }
    private boolean useDateFormatSymbols() {
        if (useDateFormatSymbols) {
            return true;
        }
        return isGregorianCalendar() || locale == null;
    }
    private boolean isGregorianCalendar() {
        return "java.util.GregorianCalendar".equals(getCalendarName());
    }
    private String translatePattern(String pattern, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (inQuote) {
                if (c == '\'')
                    inQuote = false;
            }
            else {
                if (c == '\'')
                    inQuote = true;
                else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    int ci = from.indexOf(c);
                    if (ci >= 0) {
                        if (ci < to.length()) {
                            c = to.charAt(ci);
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal pattern " +
                                                           " character '" +
                                                           c + "'");
                    }
                }
            }
            result.append(c);
        }
        if (inQuote)
            throw new IllegalArgumentException("Unfinished quote in pattern");
        return result.toString();
    }
    public String toPattern() {
        return pattern;
    }
    public String toLocalizedPattern() {
        return translatePattern(pattern,
                                DateFormatSymbols.patternChars,
                                formatData.getLocalPatternChars());
    }
    public void applyPattern(String pattern)
    {
        compiledPattern = compile(pattern);
        this.pattern = pattern;
    }
    public void applyLocalizedPattern(String pattern) {
         String p = translatePattern(pattern,
                                     formatData.getLocalPatternChars(),
                                     DateFormatSymbols.patternChars);
         compiledPattern = compile(p);
         this.pattern = p;
    }
    public DateFormatSymbols getDateFormatSymbols()
    {
        return (DateFormatSymbols)formatData.clone();
    }
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols)
    {
        this.formatData = (DateFormatSymbols)newFormatSymbols.clone();
        useDateFormatSymbols = true;
    }
    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) formatData.clone();
        return other;
    }
    public int hashCode()
    {
        return pattern.hashCode();
    }
    public boolean equals(Object obj)
    {
        if (!super.equals(obj)) return false; 
        SimpleDateFormat that = (SimpleDateFormat) obj;
        return (pattern.equals(that.pattern)
                && formatData.equals(that.formatData));
    }
    private void readObject(ObjectInputStream stream)
                         throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        try {
            compiledPattern = compile(pattern);
        } catch (Exception e) {
            throw new InvalidObjectException("invalid pattern");
        }
        if (serialVersionOnStream < 1) {
            initializeDefaultCentury();
        }
        else {
            parseAmbiguousDatesAsAfter(defaultCenturyStart);
        }
        serialVersionOnStream = currentSerialVersion;
        TimeZone tz = getTimeZone();
        if (tz instanceof SimpleTimeZone) {
            String id = tz.getID();
            TimeZone zi = TimeZone.getTimeZone(id);
            if (zi != null && zi.hasSameRules(tz) && zi.getID().equals(id)) {
                setTimeZone(zi);
            }
        }
    }
    private void checkNegativeNumberExpression() {
        if ((numberFormat instanceof DecimalFormat) &&
            !numberFormat.equals(originalNumberFormat)) {
            String numberPattern = ((DecimalFormat)numberFormat).toPattern();
            if (!numberPattern.equals(originalNumberPattern)) {
                hasFollowingMinusSign = false;
                int separatorIndex = numberPattern.indexOf(';');
                if (separatorIndex > -1) {
                    int minusIndex = numberPattern.indexOf('-', separatorIndex);
                    if ((minusIndex > numberPattern.lastIndexOf('0')) &&
                        (minusIndex > numberPattern.lastIndexOf('#'))) {
                        hasFollowingMinusSign = true;
                        minusSign = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getMinusSign();
                    }
                }
                originalNumberPattern = numberPattern;
            }
            originalNumberFormat = numberFormat;
        }
    }
}
