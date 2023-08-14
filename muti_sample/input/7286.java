public class MessageFormat extends Format {
    private static final long serialVersionUID = 6479157306784022952L;
    public MessageFormat(String pattern) {
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        applyPattern(pattern);
    }
    public MessageFormat(String pattern, Locale locale) {
        this.locale = locale;
        applyPattern(pattern);
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    public Locale getLocale() {
        return locale;
    }
    public void applyPattern(String pattern) {
            StringBuilder[] segments = new StringBuilder[4];
            segments[SEG_RAW] = new StringBuilder();
            int part = SEG_RAW;
            int formatNumber = 0;
            boolean inQuote = false;
            int braceStack = 0;
            maxOffset = -1;
            for (int i = 0; i < pattern.length(); ++i) {
                char ch = pattern.charAt(i);
                if (part == SEG_RAW) {
                    if (ch == '\'') {
                        if (i + 1 < pattern.length()
                            && pattern.charAt(i+1) == '\'') {
                            segments[part].append(ch);  
                            ++i;
                        } else {
                            inQuote = !inQuote;
                        }
                    } else if (ch == '{' && !inQuote) {
                        part = SEG_INDEX;
                        if (segments[SEG_INDEX] == null) {
                            segments[SEG_INDEX] = new StringBuilder();
                        }
                    } else {
                        segments[part].append(ch);
                    }
                } else  {
                    if (inQuote) {              
                        segments[part].append(ch);
                        if (ch == '\'') {
                            inQuote = false;
                        }
                    } else {
                        switch (ch) {
                        case ',':
                            if (part < SEG_MODIFIER) {
                                if (segments[++part] == null) {
                                    segments[part] = new StringBuilder();
                                }
                            } else {
                                segments[part].append(ch);
                            }
                            break;
                        case '{':
                            ++braceStack;
                            segments[part].append(ch);
                            break;
                        case '}':
                            if (braceStack == 0) {
                                part = SEG_RAW;
                                makeFormat(i, formatNumber, segments);
                                formatNumber++;
                                segments[SEG_INDEX] = null;
                                segments[SEG_TYPE] = null;
                                segments[SEG_MODIFIER] = null;
                            } else {
                                --braceStack;
                                segments[part].append(ch);
                            }
                            break;
                        case ' ':
                            if (part != SEG_TYPE || segments[SEG_TYPE].length() > 0) {
                                segments[part].append(ch);
                            }
                            break;
                        case '\'':
                            inQuote = true;
                        default:
                            segments[part].append(ch);
                            break;
                        }
                    }
                }
            }
            if (braceStack == 0 && part != 0) {
                maxOffset = -1;
                throw new IllegalArgumentException("Unmatched braces in the pattern.");
            }
            this.pattern = segments[0].toString();
    }
    public String toPattern() {
        int lastOffset = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= maxOffset; ++i) {
            copyAndFixQuotes(pattern, lastOffset, offsets[i], result);
            lastOffset = offsets[i];
            result.append('{').append(argumentNumbers[i]);
            Format fmt = formats[i];
            if (fmt == null) {
            } else if (fmt instanceof NumberFormat) {
                if (fmt.equals(NumberFormat.getInstance(locale))) {
                    result.append(",number");
                } else if (fmt.equals(NumberFormat.getCurrencyInstance(locale))) {
                    result.append(",number,currency");
                } else if (fmt.equals(NumberFormat.getPercentInstance(locale))) {
                    result.append(",number,percent");
                } else if (fmt.equals(NumberFormat.getIntegerInstance(locale))) {
                    result.append(",number,integer");
                } else {
                    if (fmt instanceof DecimalFormat) {
                        result.append(",number,").append(((DecimalFormat)fmt).toPattern());
                    } else if (fmt instanceof ChoiceFormat) {
                        result.append(",choice,").append(((ChoiceFormat)fmt).toPattern());
                    } else {
                    }
                }
            } else if (fmt instanceof DateFormat) {
                int index;
                for (index = MODIFIER_DEFAULT; index < DATE_TIME_MODIFIERS.length; index++) {
                    DateFormat df = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[index],
                                                               locale);
                    if (fmt.equals(df)) {
                        result.append(",date");
                        break;
                    }
                    df = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[index],
                                                    locale);
                    if (fmt.equals(df)) {
                        result.append(",time");
                        break;
                    }
                }
                if (index >= DATE_TIME_MODIFIERS.length) {
                    if (fmt instanceof SimpleDateFormat) {
                        result.append(",date,").append(((SimpleDateFormat)fmt).toPattern());
                    } else {
                    }
                } else if (index != MODIFIER_DEFAULT) {
                    result.append(',').append(DATE_TIME_MODIFIER_KEYWORDS[index]);
                }
            } else {
            }
            result.append('}');
        }
        copyAndFixQuotes(pattern, lastOffset, pattern.length(), result);
        return result.toString();
    }
    public void setFormatsByArgumentIndex(Format[] newFormats) {
        for (int i = 0; i <= maxOffset; i++) {
            int j = argumentNumbers[i];
            if (j < newFormats.length) {
                formats[i] = newFormats[j];
            }
        }
    }
    public void setFormats(Format[] newFormats) {
        int runsToCopy = newFormats.length;
        if (runsToCopy > maxOffset + 1) {
            runsToCopy = maxOffset + 1;
        }
        for (int i = 0; i < runsToCopy; i++) {
            formats[i] = newFormats[i];
        }
    }
    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        for (int j = 0; j <= maxOffset; j++) {
            if (argumentNumbers[j] == argumentIndex) {
                formats[j] = newFormat;
            }
        }
    }
    public void setFormat(int formatElementIndex, Format newFormat) {
        formats[formatElementIndex] = newFormat;
    }
    public Format[] getFormatsByArgumentIndex() {
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = argumentNumbers[i];
            }
        }
        Format[] resultArray = new Format[maximumArgumentNumber + 1];
        for (int i = 0; i <= maxOffset; i++) {
            resultArray[argumentNumbers[i]] = formats[i];
        }
        return resultArray;
    }
    public Format[] getFormats() {
        Format[] resultArray = new Format[maxOffset + 1];
        System.arraycopy(formats, 0, resultArray, 0, maxOffset + 1);
        return resultArray;
    }
    public final StringBuffer format(Object[] arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        return subformat(arguments, result, pos, null);
    }
    public static String format(String pattern, Object ... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }
    public final StringBuffer format(Object arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        return subformat((Object[]) arguments, result, pos, null);
    }
    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        StringBuffer result = new StringBuffer();
        ArrayList iterators = new ArrayList();
        if (arguments == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator must be passed non-null object");
        }
        subformat((Object[]) arguments, result, null, iterators);
        if (iterators.size() == 0) {
            return createAttributedCharacterIterator("");
        }
        return createAttributedCharacterIterator(
                     (AttributedCharacterIterator[])iterators.toArray(
                     new AttributedCharacterIterator[iterators.size()]));
    }
    public Object[] parse(String source, ParsePosition pos) {
        if (source == null) {
            Object[] empty = {};
            return empty;
        }
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = argumentNumbers[i];
            }
        }
        Object[] resultArray = new Object[maximumArgumentNumber + 1];
        int patternOffset = 0;
        int sourceOffset = pos.index;
        ParsePosition tempStatus = new ParsePosition(0);
        for (int i = 0; i <= maxOffset; ++i) {
            int len = offsets[i] - patternOffset;
            if (len == 0 || pattern.regionMatches(patternOffset,
                                                  source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
            } else {
                pos.errorIndex = sourceOffset;
                return null; 
            }
            if (formats[i] == null) {   
                int tempLength = (i != maxOffset) ? offsets[i+1] : pattern.length();
                int next;
                if (patternOffset >= tempLength) {
                    next = source.length();
                }else{
                    next = source.indexOf(pattern.substring(patternOffset, tempLength),
                                          sourceOffset);
                }
                if (next < 0) {
                    pos.errorIndex = sourceOffset;
                    return null; 
                } else {
                    String strValue= source.substring(sourceOffset,next);
                    if (!strValue.equals("{"+argumentNumbers[i]+"}"))
                        resultArray[argumentNumbers[i]]
                            = source.substring(sourceOffset,next);
                    sourceOffset = next;
                }
            } else {
                tempStatus.index = sourceOffset;
                resultArray[argumentNumbers[i]]
                    = formats[i].parseObject(source,tempStatus);
                if (tempStatus.index == sourceOffset) {
                    pos.errorIndex = sourceOffset;
                    return null; 
                }
                sourceOffset = tempStatus.index; 
            }
        }
        int len = pattern.length() - patternOffset;
        if (len == 0 || pattern.regionMatches(patternOffset,
                                              source, sourceOffset, len)) {
            pos.index = sourceOffset + len;
        } else {
            pos.errorIndex = sourceOffset;
            return null; 
        }
        return resultArray;
    }
    public Object[] parse(String source) throws ParseException {
        ParsePosition pos  = new ParsePosition(0);
        Object[] result = parse(source, pos);
        if (pos.index == 0)  
            throw new ParseException("MessageFormat parse error!", pos.errorIndex);
        return result;
    }
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }
    public Object clone() {
        MessageFormat other = (MessageFormat) super.clone();
        other.formats = (Format[]) formats.clone(); 
        for (int i = 0; i < formats.length; ++i) {
            if (formats[i] != null)
                other.formats[i] = (Format)formats[i].clone();
        }
        other.offsets = (int[]) offsets.clone();
        other.argumentNumbers = (int[]) argumentNumbers.clone();
        return other;
    }
    public boolean equals(Object obj) {
        if (this == obj)                      
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MessageFormat other = (MessageFormat) obj;
        return (maxOffset == other.maxOffset
                && pattern.equals(other.pattern)
                && ((locale != null && locale.equals(other.locale))
                 || (locale == null && other.locale == null))
                && Arrays.equals(offsets,other.offsets)
                && Arrays.equals(argumentNumbers,other.argumentNumbers)
                && Arrays.equals(formats,other.formats));
    }
    public int hashCode() {
        return pattern.hashCode(); 
    }
    public static class Field extends Format.Field {
        private static final long serialVersionUID = 7899943957617360810L;
        protected Field(String name) {
            super(name);
        }
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != MessageFormat.Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            return ARGUMENT;
        }
        public final static Field ARGUMENT =
                           new Field("message argument field");
    }
    private Locale locale;
    private String pattern = "";
    private static final int INITIAL_FORMATS = 10;
    private Format[] formats = new Format[INITIAL_FORMATS];
    private int[] offsets = new int[INITIAL_FORMATS];
    private int[] argumentNumbers = new int[INITIAL_FORMATS];
    private int maxOffset = -1;
    private StringBuffer subformat(Object[] arguments, StringBuffer result,
                                   FieldPosition fp, List characterIterators) {
        int lastOffset = 0;
        int last = result.length();
        for (int i = 0; i <= maxOffset; ++i) {
            result.append(pattern.substring(lastOffset, offsets[i]));
            lastOffset = offsets[i];
            int argumentNumber = argumentNumbers[i];
            if (arguments == null || argumentNumber >= arguments.length) {
                result.append('{').append(argumentNumber).append('}');
                continue;
            }
            if (false) { 
                result.append('\uFFFD');
            } else {
                Object obj = arguments[argumentNumber];
                String arg = null;
                Format subFormatter = null;
                if (obj == null) {
                    arg = "null";
                } else if (formats[i] != null) {
                    subFormatter = formats[i];
                    if (subFormatter instanceof ChoiceFormat) {
                        arg = formats[i].format(obj);
                        if (arg.indexOf('{') >= 0) {
                            subFormatter = new MessageFormat(arg, locale);
                            obj = arguments;
                            arg = null;
                        }
                    }
                } else if (obj instanceof Number) {
                    subFormatter = NumberFormat.getInstance(locale);
                } else if (obj instanceof Date) {
                    subFormatter = DateFormat.getDateTimeInstance(
                             DateFormat.SHORT, DateFormat.SHORT, locale);
                } else if (obj instanceof String) {
                    arg = (String) obj;
                } else {
                    arg = obj.toString();
                    if (arg == null) arg = "null";
                }
                if (characterIterators != null) {
                    if (last != result.length()) {
                        characterIterators.add(
                            createAttributedCharacterIterator(result.substring
                                                              (last)));
                        last = result.length();
                    }
                    if (subFormatter != null) {
                        AttributedCharacterIterator subIterator =
                                   subFormatter.formatToCharacterIterator(obj);
                        append(result, subIterator);
                        if (last != result.length()) {
                            characterIterators.add(
                                         createAttributedCharacterIterator(
                                         subIterator, Field.ARGUMENT,
                                         Integer.valueOf(argumentNumber)));
                            last = result.length();
                        }
                        arg = null;
                    }
                    if (arg != null && arg.length() > 0) {
                        result.append(arg);
                        characterIterators.add(
                                 createAttributedCharacterIterator(
                                 arg, Field.ARGUMENT,
                                 Integer.valueOf(argumentNumber)));
                        last = result.length();
                    }
                }
                else {
                    if (subFormatter != null) {
                        arg = subFormatter.format(obj);
                    }
                    last = result.length();
                    result.append(arg);
                    if (i == 0 && fp != null && Field.ARGUMENT.equals(
                                  fp.getFieldAttribute())) {
                        fp.setBeginIndex(last);
                        fp.setEndIndex(result.length());
                    }
                    last = result.length();
                }
            }
        }
        result.append(pattern.substring(lastOffset, pattern.length()));
        if (characterIterators != null && last != result.length()) {
            characterIterators.add(createAttributedCharacterIterator(
                                   result.substring(last)));
        }
        return result;
    }
    private void append(StringBuffer result, CharacterIterator iterator) {
        if (iterator.first() != CharacterIterator.DONE) {
            char aChar;
            result.append(iterator.first());
            while ((aChar = iterator.next()) != CharacterIterator.DONE) {
                result.append(aChar);
            }
        }
    }
    private static final int SEG_RAW      = 0;
    private static final int SEG_INDEX    = 1;
    private static final int SEG_TYPE     = 2;
    private static final int SEG_MODIFIER = 3; 
    private static final int TYPE_NULL    = 0;
    private static final int TYPE_NUMBER  = 1;
    private static final int TYPE_DATE    = 2;
    private static final int TYPE_TIME    = 3;
    private static final int TYPE_CHOICE  = 4;
    private static final String[] TYPE_KEYWORDS = {
        "",
        "number",
        "date",
        "time",
        "choice"
    };
    private static final int MODIFIER_DEFAULT  = 0; 
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_PERCENT  = 2;
    private static final int MODIFIER_INTEGER  = 3;
    private static final String[] NUMBER_MODIFIER_KEYWORDS = {
        "",
        "currency",
        "percent",
        "integer"
    };
    private static final int MODIFIER_SHORT   = 1;
    private static final int MODIFIER_MEDIUM  = 2;
    private static final int MODIFIER_LONG    = 3;
    private static final int MODIFIER_FULL    = 4;
    private static final String[] DATE_TIME_MODIFIER_KEYWORDS = {
        "",
        "short",
        "medium",
        "long",
        "full"
    };
    private static final int[] DATE_TIME_MODIFIERS = {
        DateFormat.DEFAULT,
        DateFormat.SHORT,
        DateFormat.MEDIUM,
        DateFormat.LONG,
        DateFormat.FULL,
    };
    private void makeFormat(int position, int offsetNumber,
                            StringBuilder[] textSegments)
    {
        String[] segments = new String[textSegments.length];
        for (int i = 0; i < textSegments.length; i++) {
            StringBuilder oneseg = textSegments[i];
            segments[i] = (oneseg != null) ? oneseg.toString() : "";
        }
        int argumentNumber;
        try {
            argumentNumber = Integer.parseInt(segments[SEG_INDEX]); 
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("can't parse argument number: "
                                               + segments[SEG_INDEX], e);
        }
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number: "
                                               + argumentNumber);
        }
        if (offsetNumber >= formats.length) {
            int newLength = formats.length * 2;
            Format[] newFormats = new Format[newLength];
            int[] newOffsets = new int[newLength];
            int[] newArgumentNumbers = new int[newLength];
            System.arraycopy(formats, 0, newFormats, 0, maxOffset + 1);
            System.arraycopy(offsets, 0, newOffsets, 0, maxOffset + 1);
            System.arraycopy(argumentNumbers, 0, newArgumentNumbers, 0, maxOffset + 1);
            formats = newFormats;
            offsets = newOffsets;
            argumentNumbers = newArgumentNumbers;
        }
        int oldMaxOffset = maxOffset;
        maxOffset = offsetNumber;
        offsets[offsetNumber] = segments[SEG_RAW].length();
        argumentNumbers[offsetNumber] = argumentNumber;
        Format newFormat = null;
        if (segments[SEG_TYPE].length() != 0) {
            int type = findKeyword(segments[SEG_TYPE], TYPE_KEYWORDS);
            switch (type) {
            case TYPE_NULL:
                break;
            case TYPE_NUMBER:
                switch (findKeyword(segments[SEG_MODIFIER], NUMBER_MODIFIER_KEYWORDS)) {
                case MODIFIER_DEFAULT:
                    newFormat = NumberFormat.getInstance(locale);
                    break;
                case MODIFIER_CURRENCY:
                    newFormat = NumberFormat.getCurrencyInstance(locale);
                    break;
                case MODIFIER_PERCENT:
                    newFormat = NumberFormat.getPercentInstance(locale);
                    break;
                case MODIFIER_INTEGER:
                    newFormat = NumberFormat.getIntegerInstance(locale);
                    break;
                default: 
                    try {
                        newFormat = new DecimalFormat(segments[SEG_MODIFIER],
                                                      DecimalFormatSymbols.getInstance(locale));
                    } catch (IllegalArgumentException e) {
                        maxOffset = oldMaxOffset;
                        throw e;
                    }
                    break;
                }
                break;
            case TYPE_DATE:
            case TYPE_TIME:
                int mod = findKeyword(segments[SEG_MODIFIER], DATE_TIME_MODIFIER_KEYWORDS);
                if (mod >= 0 && mod < DATE_TIME_MODIFIER_KEYWORDS.length) {
                    if (type == TYPE_DATE) {
                        newFormat = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[mod],
                                                               locale);
                    } else {
                        newFormat = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[mod],
                                                               locale);
                    }
                } else {
                    try {
                        newFormat = new SimpleDateFormat(segments[SEG_MODIFIER], locale);
                    } catch (IllegalArgumentException e) {
                        maxOffset = oldMaxOffset;
                        throw e;
                    }
                }
                break;
            case TYPE_CHOICE:
                try {
                    newFormat = new ChoiceFormat(segments[SEG_MODIFIER]);
                } catch (Exception e) {
                    maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException("Choice Pattern incorrect: "
                                                       + segments[SEG_MODIFIER], e);
                }
                break;
            default:
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("unknown format type: " +
                                                   segments[SEG_TYPE]);
            }
        }
        formats[offsetNumber] = newFormat;
    }
    private static final int findKeyword(String s, String[] list) {
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }
        String ls = s.trim().toLowerCase(Locale.ROOT);
        if (ls != s) {
            for (int i = 0; i < list.length; ++i) {
                if (ls.equals(list[i]))
                    return i;
            }
        }
        return -1;
    }
    private static final void copyAndFixQuotes(String source, int start, int end,
                                               StringBuilder target) {
        boolean quoted = false;
        for (int i = start; i < end; ++i) {
            char ch = source.charAt(i);
            if (ch == '{') {
                if (!quoted) {
                    target.append('\'');
                    quoted = true;
                }
                target.append(ch);
            } else if (ch == '\'') {
                target.append("''");
            } else {
                if (quoted) {
                    target.append('\'');
                    quoted = false;
                }
                target.append(ch);
            }
        }
        if (quoted) {
            target.append('\'');
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        boolean isValid = maxOffset >= -1
                && formats.length > maxOffset
                && offsets.length > maxOffset
                && argumentNumbers.length > maxOffset;
        if (isValid) {
            int lastOffset = pattern.length() + 1;
            for (int i = maxOffset; i >= 0; --i) {
                if ((offsets[i] < 0) || (offsets[i] > lastOffset)) {
                    isValid = false;
                    break;
                } else {
                    lastOffset = offsets[i];
                }
            }
        }
        if (!isValid) {
            throw new InvalidObjectException("Could not reconstruct MessageFormat from corrupt stream.");
        }
    }
}
