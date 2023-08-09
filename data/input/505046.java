public class MessageFormat extends Format {
    private static final long serialVersionUID = 6479157306784022952L;
    private Locale locale = Locale.getDefault();
    transient private String[] strings;
    private int[] argumentNumbers;
    private Format[] formats;
    private int maxOffset;
    transient private int maxArgumentIndex;
    public MessageFormat(String template, Locale locale) {
        this.locale = locale;
        applyPattern(template);
    }
    public MessageFormat(String template) {
        applyPattern(template);
    }
    public void applyPattern(String template) {
        int length = template.length();
        StringBuffer buffer = new StringBuffer();
        ParsePosition position = new ParsePosition(0);
        Vector<String> localStrings = new Vector<String>();
        int argCount = 0;
        int[] args = new int[10];
        int maxArg = -1;
        Vector<Format> localFormats = new Vector<Format>();
        while (position.getIndex() < length) {
            if (Format.upTo(template, position, buffer, '{')) {
                int arg = 0;
                int offset = position.getIndex();
                if (offset >= length) {
                    throw new IllegalArgumentException(Messages
                            .getString("text.19")); 
                }
                char ch;
                while ((ch = template.charAt(offset++)) != '}' && ch != ',') {
                    if (ch < '0' && ch > '9') {
                        throw new IllegalArgumentException(Messages
                            .getString("text.19")); 
                    }
                    arg = arg * 10 + (ch - '0');
                    if (arg < 0 || offset >= length) {
                        throw new IllegalArgumentException(Messages
                            .getString("text.19")); 
                    }
                }
                offset--;
                position.setIndex(offset);
                localFormats.addElement(parseVariable(template, position));
                if (argCount >= args.length) {
                    int[] newArgs = new int[args.length * 2];
                    System.arraycopy(args, 0, newArgs, 0, args.length);
                    args = newArgs;
                }
                args[argCount++] = arg;
                if (arg > maxArg) {
                    maxArg = arg;
                }
            }
            localStrings.addElement(buffer.toString());
            buffer.setLength(0);
        }
        this.strings = new String[localStrings.size()];
        for (int i = 0; i < localStrings.size(); i++) {
            this.strings[i] = localStrings.elementAt(i);
        }
        argumentNumbers = args;
        this.formats = new Format[argCount];
        for (int i = 0; i < argCount; i++) {
            this.formats[i] = localFormats.elementAt(i);
        }
        maxOffset = argCount - 1;
        maxArgumentIndex = maxArg;
    }
    @Override
    public Object clone() {
        MessageFormat clone = (MessageFormat) super.clone();
        Format[] array = new Format[formats.length];
        for (int i = formats.length; --i >= 0;) {
            if (formats[i] != null) {
                array[i] = (Format) formats[i].clone();
            }
        }
        clone.formats = array;
        return clone;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MessageFormat)) {
            return false;
        }
        MessageFormat format = (MessageFormat) object;
        if (maxOffset != format.maxOffset) {
            return false;
        }
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] != format.argumentNumbers[i]) {
                return false;
            }
        }
        return locale.equals(format.locale)
                && Arrays.equals(strings, format.strings)
                && Arrays.equals(formats, format.formats);
    }
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        StringBuffer buffer = new StringBuffer();
        Vector<FieldContainer> fields = new Vector<FieldContainer>();
        formatImpl((Object[]) object, buffer, new FieldPosition(0), fields);
        AttributedString as = new AttributedString(buffer.toString());
        for (int i = 0; i < fields.size(); i++) {
            FieldContainer fc = fields.elementAt(i);
            as.addAttribute(fc.attribute, fc.value, fc.start, fc.end);
        }
        return as.getIterator();
    }
    public final StringBuffer format(Object[] objects, StringBuffer buffer,
            FieldPosition field) {
        return formatImpl(objects, buffer, field, null);
    }
    private StringBuffer formatImpl(Object[] objects, StringBuffer buffer,
            FieldPosition position, Vector<FieldContainer> fields) {
        FieldPosition passedField = new FieldPosition(0);
        for (int i = 0; i <= maxOffset; i++) {
            buffer.append(strings[i]);
            int begin = buffer.length();
            Object arg;
            if (objects != null && argumentNumbers[i] < objects.length) {
                arg = objects[argumentNumbers[i]];
            } else {
                buffer.append('{');
                buffer.append(argumentNumbers[i]);
                buffer.append('}');
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                continue;
            }
            Format format = formats[i];
            if (format == null || arg == null) {
                if (arg instanceof Number) {
                    format = NumberFormat.getInstance();
                } else if (arg instanceof Date) {
                    format = DateFormat.getInstance();
                } else {
                    buffer.append(arg);
                    handleArgumentField(begin, buffer.length(),
                            argumentNumbers[i], position, fields);
                    continue;
                }
            }
            if (format instanceof ChoiceFormat) {
                String result = format.format(arg);
                MessageFormat mf = new MessageFormat(result);
                mf.setLocale(locale);
                mf.format(objects, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                handleformat(format, arg, begin, fields);
            } else {
                format.format(arg, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                handleformat(format, arg, begin, fields);
            }
        }
        if (maxOffset + 1 < strings.length) {
            buffer.append(strings[maxOffset + 1]);
        }
        return buffer;
    }
    private void handleArgumentField(int begin, int end, int argnumber,
            FieldPosition position, Vector<FieldContainer> fields) {
        if (fields != null) {
            fields.add(new FieldContainer(begin, end, Field.ARGUMENT,
                    new Integer(argnumber)));
        } else {
            if (position != null
                    && position.getFieldAttribute() == Field.ARGUMENT
                    && position.getEndIndex() == 0) {
                position.setBeginIndex(begin);
                position.setEndIndex(end);
            }
        }
    }
    private static class FieldContainer {
        int start, end;
        AttributedCharacterIterator.Attribute attribute;
        Object value;
        public FieldContainer(int start, int end,
                AttributedCharacterIterator.Attribute attribute, Object value) {
            this.start = start;
            this.end = end;
            this.attribute = attribute;
            this.value = value;
        }
    }
    private void handleformat(Format format, Object arg, int begin,
            Vector<FieldContainer> fields) {
        if (fields != null) {
            AttributedCharacterIterator iterator = format
                    .formatToCharacterIterator(arg);
            while (iterator.getIndex() != iterator.getEndIndex()) {
                int start = iterator.getRunStart();
                int end = iterator.getRunLimit();
                Iterator<?> it = iterator.getAttributes().keySet().iterator();
                while (it.hasNext()) {
                    AttributedCharacterIterator.Attribute attribute = (AttributedCharacterIterator.Attribute) it
                            .next();
                    Object value = iterator.getAttribute(attribute);
                    fields.add(new FieldContainer(begin + start, begin + end,
                            attribute, value));
                }
                iterator.setIndex(end);
            }
        }
    }
    @Override
    public final StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
        return format((Object[]) object, buffer, field);
    }
    public static String format(String template, Object... objects) {
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) {
                    objects[i] = "null";
                }
            }
        }
        return new MessageFormat(template).format(objects);
    }
    public Format[] getFormats() {
        return formats.clone();
    }
    public Format[] getFormatsByArgumentIndex() {
        Format[] answer = new Format[maxArgumentIndex + 1];
        for (int i = 0; i < maxOffset + 1; i++) {
            answer[argumentNumbers[i]] = formats[i];
        }
        return answer;
    }
    public void setFormatByArgumentIndex(int argIndex, Format format) {
        for (int i = 0; i < maxOffset + 1; i++) {
            if (argumentNumbers[i] == argIndex) {
                formats[i] = format;
            }
        }
    }
    public void setFormatsByArgumentIndex(Format[] formats) {
        for (int j = 0; j < formats.length; j++) {
            for (int i = 0; i < maxOffset + 1; i++) {
                if (argumentNumbers[i] == j) {
                    this.formats[i] = formats[j];
                }
            }
        }
    }
    public Locale getLocale() {
        return locale;
    }
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i <= maxOffset; i++) {
            hashCode += argumentNumbers[i] + strings[i].hashCode();
            if (formats[i] != null) {
                hashCode += formats[i].hashCode();
            }
        }
        if (maxOffset + 1 < strings.length) {
            hashCode += strings[maxOffset + 1].hashCode();
        }
        if (locale != null) {
            return hashCode + locale.hashCode();
        }
        return hashCode;
    }
    public Object[] parse(String string) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Object[] result = parse(string, position);
        if (position.getIndex() == 0) {
            throw new ParseException(
                    Messages.getString("text.1B"), position.getErrorIndex()); 
        }
        return result;
    }
    public Object[] parse(String string, ParsePosition position) {
        if (string == null) {
            return new Object[0];
        }
        ParsePosition internalPos = new ParsePosition(0);
        int offset = position.getIndex();
        Object[] result = new Object[maxArgumentIndex + 1];
        for (int i = 0; i <= maxOffset; i++) {
            String sub = strings[i];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
            Object parse;
            Format format = formats[i];
            if (format == null) {
                if (i + 1 < strings.length) {
                    int next = string.indexOf(strings[i + 1], offset);
                    if (next == -1) {
                        position.setErrorIndex(offset);
                        return null;
                    }
                    parse = string.substring(offset, next);
                    offset = next;
                } else {
                    parse = string.substring(offset);
                    offset = string.length();
                }
            } else {
                internalPos.setIndex(offset);
                parse = format.parseObject(string, internalPos);
                if (internalPos.getErrorIndex() != -1) {
                    position.setErrorIndex(offset);
                    return null;
                }
                offset = internalPos.getIndex();
            }
            result[argumentNumbers[i]] = parse;
        }
        if (maxOffset + 1 < strings.length) {
            String sub = strings[maxOffset + 1];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
        }
        position.setIndex(offset);
        return result;
    }
    @Override
    public Object parseObject(String string, ParsePosition position) {
        return parse(string, position);
    }
    private int match(String string, ParsePosition position, boolean last,
            String[] tokens) {
        int length = string.length(), offset = position.getIndex(), token = -1;
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        for (int i = tokens.length; --i >= 0;) {
            if (string.regionMatches(true, offset, tokens[i], 0, tokens[i]
                    .length())) {
                token = i;
                break;
            }
        }
        if (token == -1) {
            return -1;
        }
        offset += tokens[token].length();
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        char ch;
        if (offset < length
                && ((ch = string.charAt(offset)) == '}' || (!last && ch == ','))) {
            position.setIndex(offset + 1);
            return token;
        }
        return -1;
    }
    private Format parseVariable(String string, ParsePosition position) {
        int length = string.length(), offset = position.getIndex();
        char ch;
        if (offset >= length
                || ((ch = string.charAt(offset++)) != '}' && ch != ',')) {
            throw new IllegalArgumentException(Messages.getString("text.15")); 
        }
        position.setIndex(offset);
        if (ch == '}') {
            return null;
        }
        int type = match(string, position, false, new String[] { "time", 
                "date", "number", "choice" }); 
        if (type == -1) {
            throw new IllegalArgumentException(Messages.getString("text.16")); 
        }
        StringBuffer buffer = new StringBuffer();
        ch = string.charAt(position.getIndex() - 1);
        switch (type) {
            case 0: 
            case 1: 
                if (ch == '}') {
                    return type == 1 ? DateFormat.getDateInstance(
                            DateFormat.DEFAULT, locale) : DateFormat
                            .getTimeInstance(DateFormat.DEFAULT, locale);
                }
                int dateStyle = match(string, position, true, new String[] {
                        "full", "long", "medium", "short" }); 
                if (dateStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new SimpleDateFormat(buffer.toString(), locale);
                }
                switch (dateStyle) {
                    case 0:
                        dateStyle = DateFormat.FULL;
                        break;
                    case 1:
                        dateStyle = DateFormat.LONG;
                        break;
                    case 2:
                        dateStyle = DateFormat.MEDIUM;
                        break;
                    case 3:
                        dateStyle = DateFormat.SHORT;
                        break;
                }
                return type == 1 ? DateFormat
                        .getDateInstance(dateStyle, locale) : DateFormat
                        .getTimeInstance(dateStyle, locale);
            case 2: 
                if (ch == '}') {
                    return NumberFormat.getInstance(locale);
                }
                int numberStyle = match(string, position, true, new String[] {
                        "currency", "percent", "integer" }); 
                if (numberStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new DecimalFormat(buffer.toString(),
                            new DecimalFormatSymbols(locale));
                }
                switch (numberStyle) {
                    case 0: 
                        return NumberFormat.getCurrencyInstance(locale);
                    case 1: 
                        return NumberFormat.getPercentInstance(locale);
                }
                return NumberFormat.getIntegerInstance(locale);
        }
        try {
            Format.upToWithQuotes(string, position, buffer, '}', '{');
        } catch (IllegalArgumentException e) {
        }
        return new ChoiceFormat(buffer.toString());
    }
    public void setFormat(int offset, Format format) {
        formats[offset] = format;
    }
    public void setFormats(Format[] formats) {
        int min = this.formats.length;
        if (formats.length < min) {
            min = formats.length;
        }
        for (int i = 0; i < min; i++) {
            this.formats[i] = formats[i];
        }
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
        for (int i = 0; i <= maxOffset; i++) {
            Format format = formats[i];
            if (format instanceof DecimalFormat) {
                try {
                    formats[i] = new DecimalFormat(((DecimalFormat) format)
                            .toPattern(), new DecimalFormatSymbols(locale));
                } catch (NullPointerException npe){
                    formats[i] = null;
                }
            } else if (format instanceof SimpleDateFormat) {
                try {
                    formats[i] = new SimpleDateFormat(((SimpleDateFormat) format)
                            .toPattern(), locale);
                } catch (NullPointerException npe) {
                    formats[i] = null;
                }
            }
        }
    }
    private String decodeDecimalFormat(StringBuffer buffer, Format format) {
        buffer.append(",number"); 
        if (format.equals(NumberFormat.getNumberInstance(locale))) {
        } else if (format.equals(NumberFormat.getIntegerInstance(locale))) {
            buffer.append(",integer"); 
        } else if (format.equals(NumberFormat.getCurrencyInstance(locale))) {
            buffer.append(",currency"); 
        } else if (format.equals(NumberFormat.getPercentInstance(locale))) {
            buffer.append(",percent"); 
        } else {
            buffer.append(',');
            return ((DecimalFormat) format).toPattern();
        }
        return null;
    }
    private String decodeSimpleDateFormat(StringBuffer buffer, Format format) {
        if (format.equals(DateFormat
                .getTimeInstance(DateFormat.DEFAULT, locale))) {
            buffer.append(",time"); 
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.DEFAULT,
                locale))) {
            buffer.append(",date"); 
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",time,short"); 
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",date,short"); 
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",time,long"); 
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",date,long"); 
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",time,full"); 
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",date,full"); 
        } else {
            buffer.append(",date,"); 
            return ((SimpleDateFormat) format).toPattern();
        }
        return null;
    }
    public String toPattern() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= maxOffset; i++) {
            appendQuoted(buffer, strings[i]);
            buffer.append('{');
            buffer.append(argumentNumbers[i]);
            Format format = formats[i];
            String pattern = null;
            if (format instanceof ChoiceFormat) {
                buffer.append(",choice,"); 
                pattern = ((ChoiceFormat) format).toPattern();
            } else if (format instanceof DecimalFormat) {
                pattern = decodeDecimalFormat(buffer, format);
            } else if (format instanceof SimpleDateFormat) {
                pattern = decodeSimpleDateFormat(buffer, format);
            } else if (format != null) {
                throw new IllegalArgumentException(Messages
                        .getString("text.17")); 
            }
            if (pattern != null) {
                boolean quote = false;
                int index = 0, length = pattern.length(), count = 0;
                while (index < length) {
                    char ch = pattern.charAt(index++);
                    if (ch == '\'') {
                        quote = !quote;
                    }
                    if (!quote) {
                        if (ch == '{') {
                            count++;
                        }
                        if (ch == '}') {
                            if (count > 0) {
                                count--;
                            } else {
                                buffer.append("'}"); 
                                ch = '\'';
                            }
                        }
                    }
                    buffer.append(ch);
                }
            }
            buffer.append('}');
        }
        if (maxOffset + 1 < strings.length) {
            appendQuoted(buffer, strings[maxOffset + 1]);
        }
        return buffer.toString();
    }
    private void appendQuoted(StringBuffer buffer, String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            if (ch == '{' || ch == '}') {
                buffer.append('\'');
                buffer.append(ch);
                buffer.append('\'');
            } else {
                buffer.append(ch);
            }
        }
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("argumentNumbers", int[].class), 
            new ObjectStreamField("formats", Format[].class), 
            new ObjectStreamField("locale", Locale.class), 
            new ObjectStreamField("maxOffset", Integer.TYPE), 
            new ObjectStreamField("offsets", int[].class), 
            new ObjectStreamField("pattern", String.class), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("argumentNumbers", argumentNumbers); 
        Format[] compatibleFormats = formats;
        fields.put("formats", compatibleFormats); 
        fields.put("locale", locale); 
        fields.put("maxOffset", maxOffset); 
        int offset = 0;
        int offsetsLength = maxOffset + 1;
        int[] offsets = new int[offsetsLength];
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i <= maxOffset; i++) {
            offset += strings[i].length();
            offsets[i] = offset;
            pattern.append(strings[i]);
        }
        if (maxOffset + 1 < strings.length) {
            pattern.append(strings[maxOffset + 1]);
        }
        fields.put("offsets", offsets); 
        fields.put("pattern", pattern.toString()); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        argumentNumbers = (int[]) fields.get("argumentNumbers", null); 
        formats = (Format[]) fields.get("formats", null); 
        locale = (Locale) fields.get("locale", null); 
        maxOffset = fields.get("maxOffset", 0); 
        int[] offsets = (int[]) fields.get("offsets", null); 
        String pattern = (String) fields.get("pattern", null); 
        int length;
        if (maxOffset < 0) {
            length = pattern.length() > 0 ? 1 : 0;
        } else {
            length = maxOffset
                    + (offsets[maxOffset] == pattern.length() ? 1 : 2);
        }
        strings = new String[length];
        int last = 0;
        for (int i = 0; i <= maxOffset; i++) {
            strings[i] = pattern.substring(last, offsets[i]);
            last = offsets[i];
        }
        if (maxOffset + 1 < strings.length) {
            strings[strings.length - 1] = pattern.substring(last, pattern
                    .length());
        }
    }
    public static class Field extends Format.Field {
        private static final long serialVersionUID = 7899943957617360810L;
        public static final Field ARGUMENT = new Field("message argument field"); 
        protected Field(String fieldName) {
            super(fieldName);
        }
        @Override
        protected Object readResolve() throws InvalidObjectException {
            String name = this.getName();
            if (name == null) {
                throw new InvalidObjectException(Messages.getString(
                        "text.18", "MessageFormat.Field")); 
            }
            if (name.equals(ARGUMENT.getName())) {
                return ARGUMENT;
            }
            throw new InvalidObjectException(Messages.getString(
                    "text.18", "MessageFormat.Field")); 
        }
    }
}
