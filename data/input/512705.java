public class SimpleDateFormat extends DateFormat {
    private static final long serialVersionUID = 4774881970558875024L;
    static final String patternChars = "GyMdkHmsSEDFwWahKzZ"; 
    private String pattern;
    private DateFormatSymbols formatData;
    transient private int creationYear;
    private Date defaultCenturyStart;
    public SimpleDateFormat() {
        this(Locale.getDefault());
        pattern = defaultPattern();
        formatData = new DateFormatSymbols(Locale.getDefault());
    }
    public SimpleDateFormat(String pattern) {
        this(pattern, Locale.getDefault());
    }
    private void validateFormat(char format) {
        int index = patternChars.indexOf(format);
        if (index == -1) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); 
        }
    }
    private void validatePattern(String template) {
        boolean quote = false;
        int next, last = -1, count = 0;
        final int patternLength = template.length();
        for (int i = 0; i < patternLength; i++) {
            next = (template.charAt(i));
            if (next == '\'') {
                if (count > 0) {
                    validateFormat((char) last);
                    count = 0;
                }
                if (last == next) {
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        validateFormat((char) last);
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    validateFormat((char) last);
                    count = 0;
                }
                last = -1;
            }
        }
        if (count > 0) {
            validateFormat((char) last);
        }
        if (quote) {
            throw new IllegalArgumentException(Messages.getString("text.04")); 
        }
    }
    public SimpleDateFormat(String template, DateFormatSymbols value) {
        this(Locale.getDefault());
        validatePattern(template);
        pattern = template;
        formatData = (DateFormatSymbols) value.clone();
    }
    public SimpleDateFormat(String template, Locale locale) {
        this(locale);
        validatePattern(template);
        pattern = template;
        formatData = new DateFormatSymbols(locale);
    }
    private SimpleDateFormat(Locale locale) {
        numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setParseIntegerOnly(true);
        numberFormat.setGroupingUsed(false);
        calendar = new GregorianCalendar(locale);
        calendar.add(Calendar.YEAR, -80);
        creationYear = calendar.get(Calendar.YEAR);
        defaultCenturyStart = calendar.getTime();
    }
    public void applyLocalizedPattern(String template) {
        pattern = convertPattern(template, formatData.getLocalPatternChars(),
                patternChars, true);
    }
    public void applyPattern(String template) {
        validatePattern(template);
        pattern = template;
    }
    @SuppressWarnings("nls")
    private String patternForICU(String p) {
        String[] subPatterns = p.split("'");
        boolean quote = false;
        boolean first = true;
        StringBuilder result = new StringBuilder();
        for (String subPattern : subPatterns) {
            if (!quote) {
                result.append((first ? "" : "'")
                        + subPattern.replaceAll("(?<!y)y(?!y)", "yy"));
                first = false;
            } else {
                result.append("'" + subPattern);
            }
            quote = !quote;
        }
        if (p.endsWith("'")) {
            result.append("'");
        }
        return result.toString();
    }
    @Override
    public Object clone() {
        SimpleDateFormat clone = (SimpleDateFormat) super.clone();
        clone.formatData = (DateFormatSymbols) formatData.clone();
        clone.defaultCenturyStart = new Date(defaultCenturyStart.getTime());
        return clone;
    }
    private static String defaultPattern() {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(Locale.getDefault());
        return localeData.getDateFormat(SHORT) + " " + localeData.getTimeFormat(SHORT);
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SimpleDateFormat)) {
            return false;
        }
        SimpleDateFormat simple = (SimpleDateFormat) object;
        return super.equals(object) && pattern.equals(simple.pattern)
                && formatData.equals(simple.formatData);
    }
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (object instanceof Date) {
            return formatToCharacterIteratorImpl((Date) object);
        }
        if (object instanceof Number) {
            return formatToCharacterIteratorImpl(new Date(((Number) object)
                    .longValue()));
        }
        throw new IllegalArgumentException();
    }
    private AttributedCharacterIterator formatToCharacterIteratorImpl(Date date) {
        StringBuffer buffer = new StringBuffer();
        Vector<FieldPosition> fields = new Vector<FieldPosition>();
        formatImpl(date, buffer, null, fields);
        AttributedString as = new AttributedString(buffer.toString());
        for (int i = 0; i < fields.size(); i++) {
            FieldPosition pos = fields.elementAt(i);
            Format.Field attribute = pos.getFieldAttribute();
            as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos
                    .getEndIndex());
        }
        return as.getIterator();
    }
    private StringBuffer formatImpl(Date date, StringBuffer buffer,
            FieldPosition field, Vector<FieldPosition> fields) {
        boolean quote = false;
        int next, last = -1, count = 0;
        calendar.setTime(date);
        if (field != null) {
            field.clear();
        }
        final int patternLength = pattern.length();
        for (int i = 0; i < patternLength; i++) {
            next = (pattern.charAt(i));
            if (next == '\'') {
                if (count > 0) {
                    append(buffer, field, fields, (char) last, count);
                    count = 0;
                }
                if (last == next) {
                    buffer.append('\'');
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        append(buffer, field, fields, (char) last, count);
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    append(buffer, field, fields, (char) last, count);
                    count = 0;
                }
                last = -1;
                buffer.append((char) next);
            }
        }
        if (count > 0) {
            append(buffer, field, fields, (char) last, count);
        }
        return buffer;
    }
    private void append(StringBuffer buffer, FieldPosition position,
            Vector<FieldPosition> fields, char format, int count) {
        int field = -1;
        int index = patternChars.indexOf(format);
        if (index == -1) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); 
        }
        int beginPosition = buffer.length();
        Field dateFormatField = null;
        switch (index) {
            case ERA_FIELD:
                dateFormatField = Field.ERA;
                buffer.append(formatData.eras[calendar.get(Calendar.ERA)]);
                break;
            case YEAR_FIELD:
                dateFormatField = Field.YEAR;
                int year = calendar.get(Calendar.YEAR);
                if (count == 2) {
                    appendNumber(buffer, 2, year %= 100);
                } else {
                    appendNumber(buffer, count, year);
                }
                break;
            case MONTH_FIELD:
                dateFormatField = Field.MONTH;
                int month = calendar.get(Calendar.MONTH);
                if (count <= 2) {
                    appendNumber(buffer, count, month + 1);
                } else if (count == 3) {
                    buffer.append(formatData.shortMonths[month]);
                } else {
                    buffer.append(formatData.months[month]);
                }
                break;
            case DATE_FIELD:
                dateFormatField = Field.DAY_OF_MONTH;
                field = Calendar.DATE;
                break;
            case HOUR_OF_DAY1_FIELD: 
                dateFormatField = Field.HOUR_OF_DAY1;
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                appendNumber(buffer, count, hour == 0 ? 24 : hour);
                break;
            case HOUR_OF_DAY0_FIELD: 
                dateFormatField = Field.HOUR_OF_DAY0;
                field = Calendar.HOUR_OF_DAY;
                break;
            case MINUTE_FIELD:
                dateFormatField = Field.MINUTE;
                field = Calendar.MINUTE;
                break;
            case SECOND_FIELD:
                dateFormatField = Field.SECOND;
                field = Calendar.SECOND;
                break;
            case MILLISECOND_FIELD:
                dateFormatField = Field.MILLISECOND;
                int value = calendar.get(Calendar.MILLISECOND);
                appendNumber(buffer, count, value);
                break;
            case DAY_OF_WEEK_FIELD:
                dateFormatField = Field.DAY_OF_WEEK;
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                if (count < 4) {
                    buffer.append(formatData.shortWeekdays[day]);
                } else {
                    buffer.append(formatData.weekdays[day]);
                }
                break;
            case DAY_OF_YEAR_FIELD:
                dateFormatField = Field.DAY_OF_YEAR;
                field = Calendar.DAY_OF_YEAR;
                break;
            case DAY_OF_WEEK_IN_MONTH_FIELD:
                dateFormatField = Field.DAY_OF_WEEK_IN_MONTH;
                field = Calendar.DAY_OF_WEEK_IN_MONTH;
                break;
            case WEEK_OF_YEAR_FIELD:
                dateFormatField = Field.WEEK_OF_YEAR;
                field = Calendar.WEEK_OF_YEAR;
                break;
            case WEEK_OF_MONTH_FIELD:
                dateFormatField = Field.WEEK_OF_MONTH;
                field = Calendar.WEEK_OF_MONTH;
                break;
            case AM_PM_FIELD:
                dateFormatField = Field.AM_PM;
                buffer.append(formatData.ampms[calendar.get(Calendar.AM_PM)]);
                break;
            case HOUR1_FIELD: 
                dateFormatField = Field.HOUR1;
                hour = calendar.get(Calendar.HOUR);
                appendNumber(buffer, count, hour == 0 ? 12 : hour);
                break;
            case HOUR0_FIELD: 
                dateFormatField = Field.HOUR0;
                field = Calendar.HOUR;
                break;
            case TIMEZONE_FIELD: 
                dateFormatField = Field.TIME_ZONE;
                appendTimeZone(buffer, count, true);
                break;
            case (TIMEZONE_FIELD + 1): 
                dateFormatField = Field.TIME_ZONE;
                appendNumericTimeZone(buffer, false);
                break;
        }
        if (field != -1) {
            appendNumber(buffer, count, calendar.get(field));
        }
        if (fields != null) {
            position = new FieldPosition(dateFormatField);
            position.setBeginIndex(beginPosition);
            position.setEndIndex(buffer.length());
            fields.add(position);
        } else {
            if ((position.getFieldAttribute() == dateFormatField || (position
                    .getFieldAttribute() == null && position.getField() == index))
                    && position.getEndIndex() == 0) {
                position.setBeginIndex(beginPosition);
                position.setEndIndex(buffer.length());
            }
        }
    }
    private void appendTimeZone(StringBuffer buffer, int count, boolean generalTimeZone) {
        if (generalTimeZone) {
            TimeZone tz = calendar.getTimeZone();
            boolean daylight = (calendar.get(Calendar.DST_OFFSET) != 0);
            int style = count < 4 ? TimeZone.SHORT : TimeZone.LONG;
            if (!formatData.customZoneStrings) {
                buffer.append(tz.getDisplayName(daylight, style, formatData.locale));
                return;
            }
            String custom = Resources.lookupDisplayTimeZone(formatData.zoneStrings, tz.getID(), daylight, style);
            if (custom != null) {
                buffer.append(custom);
                return;
            }
        }
        appendNumericTimeZone(buffer, generalTimeZone);
    }
    private void appendNumericTimeZone(StringBuffer buffer, boolean generalTimeZone) {
        int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        char sign = '+';
        if (offset < 0) {
            sign = '-';
            offset = -offset;
        }
        if (generalTimeZone) {
            buffer.append("GMT");
        }
        buffer.append(sign);
        appendNumber(buffer, 2, offset / 3600000);
        if (generalTimeZone) {
            buffer.append(':');
        }
        appendNumber(buffer, 2, (offset % 3600000) / 60000);
    }
    private void appendNumber(StringBuffer buffer, int count, int value) {
        int minimumIntegerDigits = numberFormat.getMinimumIntegerDigits();
        numberFormat.setMinimumIntegerDigits(count);
        numberFormat.format(Integer.valueOf(value), buffer, new FieldPosition(0));
        numberFormat.setMinimumIntegerDigits(minimumIntegerDigits);
    }
    private Date error(ParsePosition position, int offset, TimeZone zone) {
        position.setErrorIndex(offset);
        calendar.setTimeZone(zone);
        return null;
    }
    @Override
    public StringBuffer format(Date date, StringBuffer buffer,
            FieldPosition fieldPos) {
        return formatImpl(date, buffer, fieldPos, null);
    }
    public Date get2DigitYearStart() {
        return defaultCenturyStart;
    }
    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols) formatData.clone();
    }
    @Override
    public int hashCode() {
        return super.hashCode() + pattern.hashCode() + formatData.hashCode()
                + creationYear;
    }
    private int parse(String string, int offset, char format, int count) {
        int index = patternChars.indexOf(format);
        if (index == -1) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); 
        }
        int field = -1;
        int absolute = 0;
        if (count < 0) {
            count = -count;
            absolute = count;
        }
        switch (index) {
            case ERA_FIELD:
                return parseText(string, offset, formatData.eras, Calendar.ERA);
            case YEAR_FIELD:
                if (count >= 3) {
                    field = Calendar.YEAR;
                } else {
                    ParsePosition position = new ParsePosition(offset);
                    Number result = parseNumber(absolute, string, position);
                    if (result == null) {
                        return -position.getErrorIndex() - 1;
                    }
                    int year = result.intValue();
                    if ((position.getIndex() - offset) == 2 && year >= 0) {
                        year += creationYear / 100 * 100;
                        if (year < creationYear) {
                            year += 100;
                        }
                    }
                    calendar.set(Calendar.YEAR, year);
                    return position.getIndex();
                }
                break;
            case MONTH_FIELD:
                if (count <= 2) {
                    return parseNumber(absolute, string, offset,
                            Calendar.MONTH, -1);
                }
                index = parseText(string, offset, formatData.months,
                        Calendar.MONTH);
                if (index < 0) {
                    return parseText(string, offset, formatData.shortMonths,
                            Calendar.MONTH);
                }
                return index;
            case DATE_FIELD:
                field = Calendar.DATE;
                break;
            case HOUR_OF_DAY1_FIELD:
                ParsePosition position = new ParsePosition(offset);
                Number result = parseNumber(absolute, string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                int hour = result.intValue();
                if (hour == 24) {
                    hour = 0;
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                return position.getIndex();
            case HOUR_OF_DAY0_FIELD:
                field = Calendar.HOUR_OF_DAY;
                break;
            case MINUTE_FIELD:
                field = Calendar.MINUTE;
                break;
            case SECOND_FIELD:
                field = Calendar.SECOND;
                break;
            case MILLISECOND_FIELD:
                field = Calendar.MILLISECOND;
                break;
            case DAY_OF_WEEK_FIELD:
                index = parseText(string, offset, formatData.weekdays,
                        Calendar.DAY_OF_WEEK);
                if (index < 0) {
                    return parseText(string, offset, formatData.shortWeekdays,
                            Calendar.DAY_OF_WEEK);
                }
                return index;
            case DAY_OF_YEAR_FIELD:
                field = Calendar.DAY_OF_YEAR;
                break;
            case DAY_OF_WEEK_IN_MONTH_FIELD:
                field = Calendar.DAY_OF_WEEK_IN_MONTH;
                break;
            case WEEK_OF_YEAR_FIELD:
                field = Calendar.WEEK_OF_YEAR;
                break;
            case WEEK_OF_MONTH_FIELD:
                field = Calendar.WEEK_OF_MONTH;
                break;
            case AM_PM_FIELD:
                return parseText(string, offset, formatData.ampms,
                        Calendar.AM_PM);
            case HOUR1_FIELD:
                position = new ParsePosition(offset);
                result = parseNumber(absolute, string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                hour = result.intValue();
                if (hour == 12) {
                    hour = 0;
                }
                calendar.set(Calendar.HOUR, hour);
                return position.getIndex();
            case HOUR0_FIELD:
                field = Calendar.HOUR;
                break;
            case TIMEZONE_FIELD:
                return parseTimeZone(string, offset);
            case (TIMEZONE_FIELD + 1):
                return parseTimeZone(string, offset);
        }
        if (field != -1) {
            return parseNumber(absolute, string, offset, field, 0);
        }
        return offset;
    }
    @Override
    public Date parse(String string, ParsePosition position) {
        boolean quote = false;
        int next, last = -1, count = 0, offset = position.getIndex();
        int length = string.length();
        calendar.clear();
        TimeZone zone = calendar.getTimeZone();
        final int patternLength = pattern.length();
        for (int i = 0; i < patternLength; i++) {
            next = pattern.charAt(i);
            if (next == '\'') {
                if (count > 0) {
                    if ((offset = parse(string, offset, (char) last, count)) < 0) {
                        return error(position, -offset - 1, zone);
                    }
                    count = 0;
                }
                if (last == next) {
                    if (offset >= length || string.charAt(offset) != '\'') {
                        return error(position, offset, zone);
                    }
                    offset++;
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        if ((offset = parse(string, offset, (char) last, -count)) < 0) {
                            return error(position, -offset - 1, zone);
                        }
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    if ((offset = parse(string, offset, (char) last, count)) < 0) {
                        return error(position, -offset - 1, zone);
                    }
                    count = 0;
                }
                last = -1;
                if (offset >= length || string.charAt(offset) != next) {
                    return error(position, offset, zone);
                }
                offset++;
            }
        }
        if (count > 0) {
            if ((offset = parse(string, offset, (char) last, count)) < 0) {
                return error(position, -offset - 1, zone);
            }
        }
        Date date;
        try {
            date = calendar.getTime();
        } catch (IllegalArgumentException e) {
            return error(position, offset, zone);
        }
        position.setIndex(offset);
        calendar.setTimeZone(zone);
        return date;
    }
    private Number parseNumber(int max, String string, ParsePosition position) {
        int digit, length = string.length(), result = 0;
        int index = position.getIndex();
        if (max > 0 && max < length - index) {
            length = index + max;
        }
        while (index < length
                && (string.charAt(index) == ' ' || string.charAt(index) == '\t')) {
            index++;
        }
        if (max == 0) {
            position.setIndex(index);
            return numberFormat.parse(string, position);
        }
        while (index < length
                && (digit = Character.digit(string.charAt(index), 10)) != -1) {
            index++;
            result = result * 10 + digit;
        }
        if (index == position.getIndex()) {
            position.setErrorIndex(index);
            return null;
        }
        position.setIndex(index);
        return Integer.valueOf(result);
    }
    private int parseNumber(int max, String string, int offset, int field,
            int skew) {
        ParsePosition position = new ParsePosition(offset);
        Number result = parseNumber(max, string, position);
        if (result == null) {
            return -position.getErrorIndex() - 1;
        }
        calendar.set(field, result.intValue() + skew);
        return position.getIndex();
    }
    private int parseText(String string, int offset, String[] text, int field) {
        int found = -1;
        for (int i = 0; i < text.length; i++) {
            if (text[i].length() == 0) {
                continue;
            }
            if (string
                    .regionMatches(true, offset, text[i], 0, text[i].length())) {
                if (found == -1 || text[i].length() > text[found].length()) {
                    found = i;
                }
            }
        }
        if (found != -1) {
            calendar.set(field, found);
            return offset + text[found].length();
        }
        return -offset - 1;
    }
    private int parseTimeZone(String string, int offset) {
        String[][] zones = formatData.internalZoneStrings();
        boolean foundGMT = string.regionMatches(offset, "GMT", 0, 3); 
        if (foundGMT) {
            offset += 3;
        }
        char sign;
        if (offset < string.length()
                && ((sign = string.charAt(offset)) == '+' || sign == '-')) {
            ParsePosition position = new ParsePosition(offset + 1);
            Number result = numberFormat.parse(string, position);
            if (result == null) {
                return -position.getErrorIndex() - 1;
            }
            int hour = result.intValue();
            int raw = hour * 3600000;
            int index = position.getIndex();
            if (index < string.length() && string.charAt(index) == ':') {
                position.setIndex(index + 1);
                result = numberFormat.parse(string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                int minute = result.intValue();
                raw += minute * 60000;
            } else if (hour >= 24) {
                raw = (hour / 100 * 3600000) + (hour % 100 * 60000);
            }
            if (sign == '-') {
                raw = -raw;
            }
            calendar.setTimeZone(new SimpleTimeZone(raw, "")); 
            return position.getIndex();
        }
        if (foundGMT) {
            calendar.setTimeZone(TimeZone.getTimeZone("GMT")); 
            return offset;
        }
        for (String[] element : zones) {
            for (int j = 1; j < 5; j++) {
                if (string.regionMatches(true, offset, element[j], 0,
                        element[j].length())) {
                    TimeZone zone = TimeZone.getTimeZone(element[0]);
                    if (zone == null) {
                        return -offset - 1;
                    }
                    int raw = zone.getRawOffset();
                    if (j >= 3 && zone.useDaylightTime()) {
                        raw += 3600000;
                    }
                    calendar.setTimeZone(new SimpleTimeZone(raw, "")); 
                    return offset + element[j].length();
                }
            }
        }
        return -offset - 1;
    }
    public void set2DigitYearStart(Date date) {
        defaultCenturyStart = date;
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        creationYear = cal.get(Calendar.YEAR);
    }
    public void setDateFormatSymbols(DateFormatSymbols value) {
        formatData = (DateFormatSymbols) value.clone();
    }
    public String toLocalizedPattern() {
        return convertPattern(pattern, patternChars, formatData
                .getLocalPatternChars(), false);
    }
    public String toPattern() {
        return pattern;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("defaultCenturyStart", Date.class), 
            new ObjectStreamField("formatData", DateFormatSymbols.class), 
            new ObjectStreamField("pattern", String.class), 
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("defaultCenturyStart", defaultCenturyStart); 
        fields.put("formatData", formatData); 
        fields.put("pattern", pattern); 
        fields.put("serialVersionOnStream", 1); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        int version = fields.get("serialVersionOnStream", 0); 
        Date date;
        if (version > 0) {
            date = (Date) fields.get("defaultCenturyStart", new Date()); 
        } else {
            date = new Date();
        }
        set2DigitYearStart(date);
        formatData = (DateFormatSymbols) fields.get("formatData", null); 
        pattern = (String) fields.get("pattern", ""); 
    }
}
