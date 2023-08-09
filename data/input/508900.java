public class BasicHeaderValueParser implements HeaderValueParser {
    public final static
        BasicHeaderValueParser DEFAULT = new BasicHeaderValueParser();
    private final static char PARAM_DELIMITER                = ';';
    private final static char ELEM_DELIMITER                 = ',';
    private final static char[] ALL_DELIMITERS               = new char[] {
                                                                PARAM_DELIMITER, 
                                                                ELEM_DELIMITER
                                                                };  
    public final static
        HeaderElement[] parseElements(final String value,
                                      HeaderValueParser parser)
        throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }
        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseElements(buffer, cursor);
    }
    public HeaderElement[] parseElements(final CharArrayBuffer buffer,
                                         final ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        List elements = new ArrayList(); 
        while (!cursor.atEnd()) {
            HeaderElement element = parseHeaderElement(buffer, cursor);
            if (!(element.getName().length() == 0 && element.getValue() == null)) {
                elements.add(element);
            }
        }
        return (HeaderElement[])
            elements.toArray(new HeaderElement[elements.size()]);
    }
    public final static
        HeaderElement parseHeaderElement(final String value,
                                         HeaderValueParser parser)
        throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }
        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseHeaderElement(buffer, cursor);
    }
    public HeaderElement parseHeaderElement(final CharArrayBuffer buffer,
                                            final ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        NameValuePair nvp = parseNameValuePair(buffer, cursor);
        NameValuePair[] params = null;
        if (!cursor.atEnd()) {
            char ch = buffer.charAt(cursor.getPos() - 1); 
            if (ch != ELEM_DELIMITER) {
                params = parseParameters(buffer, cursor);
            }
        }
        return createHeaderElement(nvp.getName(), nvp.getValue(), params);
    }
    protected HeaderElement createHeaderElement(
            final String name, 
            final String value, 
            final NameValuePair[] params) {
        return new BasicHeaderElement(name, value, params);
    }
    public final static
        NameValuePair[] parseParameters(final String value,
                                        HeaderValueParser parser)
        throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }
        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseParameters(buffer, cursor);
    }
    public NameValuePair[] parseParameters(final CharArrayBuffer buffer,
                                           final ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (HTTP.isWhitespace(ch)) {
                pos++;
            } else {
                break;
            }
        }
        cursor.updatePos(pos);
        if (cursor.atEnd()) {
            return new NameValuePair[] {};
        }
        List params = new ArrayList(); 
        while (!cursor.atEnd()) {
            NameValuePair param = parseNameValuePair(buffer, cursor);
            params.add(param);
            char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch == ELEM_DELIMITER) {
                break;
            }
        }
        return (NameValuePair[])
            params.toArray(new NameValuePair[params.size()]);
    }
    public final static
       NameValuePair parseNameValuePair(final String value,
                                        HeaderValueParser parser)
        throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }
        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseNameValuePair(buffer, cursor);
    }
    public NameValuePair parseNameValuePair(final CharArrayBuffer buffer,
                                            final ParserCursor cursor) {
        return parseNameValuePair(buffer, cursor, ALL_DELIMITERS);
    }
    private static boolean isOneOf(final char ch, final char[] chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                if (ch == chs[i]) {
                    return true;
                }
            }
        }
        return false;
    }
    public NameValuePair parseNameValuePair(final CharArrayBuffer buffer,
                                            final ParserCursor cursor,
                                            final char[] delimiters) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        boolean terminated = false;
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        String name = null;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '=') {
                break;
            }
            if (isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            pos++;
        }
        if (pos == indexTo) {
            terminated = true;
            name = buffer.substringTrimmed(indexFrom, indexTo);
        } else {
            name = buffer.substringTrimmed(indexFrom, pos);
            pos++;
        }
        if (terminated) {
            cursor.updatePos(pos);
            return createNameValuePair(name, null);
        }
        String value = null;
        int i1 = pos;
        boolean qouted = false;
        boolean escaped = false;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '"' && !escaped) {
                qouted = !qouted;
            }
            if (!qouted && !escaped && isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            if (escaped) {
                escaped = false;
            } else {
                escaped = qouted && ch == '\\';
            }
            pos++;
        }
        int i2 = pos;
        while (i1 < i2 && (HTTP.isWhitespace(buffer.charAt(i1)))) {
            i1++;
        }
        while ((i2 > i1) && (HTTP.isWhitespace(buffer.charAt(i2 - 1)))) {
            i2--;
        }
        if (((i2 - i1) >= 2) 
            && (buffer.charAt(i1) == '"') 
            && (buffer.charAt(i2 - 1) == '"')) {
            i1++;
            i2--;
        }
        value = buffer.substring(i1, i2);
        if (terminated) {
            pos++;
        }
        cursor.updatePos(pos);
        return createNameValuePair(name, value);
    }
    protected NameValuePair createNameValuePair(final String name, final String value) {
        return new BasicNameValuePair(name, value);
    }
}
