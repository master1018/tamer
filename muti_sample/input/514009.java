public abstract class Format implements Serializable, Cloneable {
    private static final long serialVersionUID = -299282585814624189L;
    public Format() {
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    String convertPattern(String template, String fromChars, String toChars,
            boolean check) {
        if (!check && fromChars.equals(toChars)) {
            return template;
        }
        boolean quote = false;
        StringBuilder output = new StringBuilder();
        int length = template.length();
        for (int i = 0; i < length; i++) {
            int index;
            char next = template.charAt(i);
            if (next == '\'') {
                quote = !quote;
            }
            if (!quote && (index = fromChars.indexOf(next)) != -1) {
                output.append(toChars.charAt(index));
            } else if (check
                    && !quote
                    && ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                throw new IllegalArgumentException(Messages.getString(
                        "text.05", String.valueOf(next), template)); 
            } else {
                output.append(next);
            }
        }
        if (quote) {
            throw new IllegalArgumentException(Messages.getString("text.04")); 
        }
        return output.toString();
    }
    public final String format(Object object) {
        return format(object, new StringBuffer(), new FieldPosition(0))
                .toString();
    }
    public abstract StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field);
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        return new AttributedString(format(object)).getIterator();
    }
    public Object parseObject(String string) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Object result = parseObject(string, position);
        if (position.getIndex() == 0) {
            throw new ParseException(
                    Messages.getString("text.1C"), position.getErrorIndex()); 
        }
        return result;
    }
    public abstract Object parseObject(String string, ParsePosition position);
    static boolean upTo(String string, ParsePosition position,
            StringBuffer buffer, char stop) {
        int index = position.getIndex(), length = string.length();
        boolean lastQuote = false, quote = false;
        while (index < length) {
            char ch = string.charAt(index++);
            if (ch == '\'') {
                if (lastQuote) {
                    buffer.append('\'');
                }
                quote = !quote;
                lastQuote = true;
            } else if (ch == stop && !quote) {
                position.setIndex(index);
                return true;
            } else {
                lastQuote = false;
                buffer.append(ch);
            }
        }
        position.setIndex(index);
        return false;
    }
    static boolean upToWithQuotes(String string, ParsePosition position,
            StringBuffer buffer, char stop, char start) {
        int index = position.getIndex(), length = string.length(), count = 1;
        boolean quote = false;
        while (index < length) {
            char ch = string.charAt(index++);
            if (ch == '\'') {
                quote = !quote;
            }
            if (!quote) {
                if (ch == stop) {
                    count--;
                }
                if (count == 0) {
                    position.setIndex(index);
                    return true;
                }
                if (ch == start) {
                    count++;
                }
            }
            buffer.append(ch);
        }
        throw new IllegalArgumentException(Messages.getString("text.07")); 
    }
    public static class Field extends AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = 276966692217360283L;
        protected Field(String fieldName) {
            super(fieldName);
        }
    }
}
