class PatternEntry {
    public void appendQuotedExtension(StringBuffer toAddTo) {
        appendQuoted(extension,toAddTo);
    }
    public void appendQuotedChars(StringBuffer toAddTo) {
        appendQuoted(chars,toAddTo);
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        PatternEntry other = (PatternEntry) obj;
        boolean result = chars.equals(other.chars);
        return result;
    }
    public int hashCode() {
        return chars.hashCode();
    }
    public String toString() {
        StringBuffer result = new StringBuffer();
        addToBuffer(result, true, false, null);
        return result.toString();
    }
    final int getStrength() {
        return strength;
    }
    final String getExtension() {
        return extension;
    }
    final String getChars() {
        return chars;
    }
    void addToBuffer(StringBuffer toAddTo,
                     boolean showExtension,
                     boolean showWhiteSpace,
                     PatternEntry lastEntry)
    {
        if (showWhiteSpace && toAddTo.length() > 0)
            if (strength == Collator.PRIMARY || lastEntry != null)
                toAddTo.append('\n');
            else
                toAddTo.append(' ');
        if (lastEntry != null) {
            toAddTo.append('&');
            if (showWhiteSpace)
                toAddTo.append(' ');
            lastEntry.appendQuotedChars(toAddTo);
            appendQuotedExtension(toAddTo);
            if (showWhiteSpace)
                toAddTo.append(' ');
        }
        switch (strength) {
        case Collator.IDENTICAL: toAddTo.append('='); break;
        case Collator.TERTIARY:  toAddTo.append(','); break;
        case Collator.SECONDARY: toAddTo.append(';'); break;
        case Collator.PRIMARY:   toAddTo.append('<'); break;
        case RESET: toAddTo.append('&'); break;
        case UNSET: toAddTo.append('?'); break;
        }
        if (showWhiteSpace)
            toAddTo.append(' ');
        appendQuoted(chars,toAddTo);
        if (showExtension && extension.length() != 0) {
            toAddTo.append('/');
            appendQuoted(extension,toAddTo);
        }
    }
    static void appendQuoted(String chars, StringBuffer toAddTo) {
        boolean inQuote = false;
        char ch = chars.charAt(0);
        if (Character.isSpaceChar(ch)) {
            inQuote = true;
            toAddTo.append('\'');
        } else {
          if (PatternEntry.isSpecialChar(ch)) {
                inQuote = true;
                toAddTo.append('\'');
            } else {
                switch (ch) {
                    case 0x0010: case '\f': case '\r':
                    case '\t': case '\n':  case '@':
                    inQuote = true;
                    toAddTo.append('\'');
                    break;
                case '\'':
                    inQuote = true;
                    toAddTo.append('\'');
                    break;
                default:
                    if (inQuote) {
                        inQuote = false; toAddTo.append('\'');
                    }
                    break;
                }
           }
        }
        toAddTo.append(chars);
        if (inQuote)
            toAddTo.append('\'');
    }
    PatternEntry(int strength,
                 StringBuffer chars,
                 StringBuffer extension)
    {
        this.strength = strength;
        this.chars = chars.toString();
        this.extension = (extension.length() > 0) ? extension.toString()
                                                  : "";
    }
    static class Parser {
        private String pattern;
        private int i;
        public Parser(String pattern) {
            this.pattern = pattern;
            this.i = 0;
        }
        public PatternEntry next() throws ParseException {
            int newStrength = UNSET;
            newChars.setLength(0);
            newExtension.setLength(0);
            boolean inChars = true;
            boolean inQuote = false;
        mainLoop:
            while (i < pattern.length()) {
                char ch = pattern.charAt(i);
                if (inQuote) {
                    if (ch == '\'') {
                        inQuote = false;
                    } else {
                        if (newChars.length() == 0) newChars.append(ch);
                        else if (inChars) newChars.append(ch);
                        else newExtension.append(ch);
                    }
                } else switch (ch) {
                case '=': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.IDENTICAL; break;
                case ',': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.TERTIARY; break;
                case ';': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.SECONDARY; break;
                case '<': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.PRIMARY; break;
                case '&': if (newStrength != UNSET) break mainLoop;
                    newStrength = RESET; break;
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ': break; 
                case '/': inChars = false; break;
                case '\'':
                    inQuote = true;
                    ch = pattern.charAt(++i);
                    if (newChars.length() == 0) newChars.append(ch);
                    else if (inChars) newChars.append(ch);
                    else newExtension.append(ch);
                    break;
                default:
                    if (newStrength == UNSET) {
                        throw new ParseException
                            ("missing char (=,;<&) : " +
                             pattern.substring(i,
                                (i+10 < pattern.length()) ?
                                 i+10 : pattern.length()),
                             i);
                    }
                    if (PatternEntry.isSpecialChar(ch) && (inQuote == false))
                        throw new ParseException
                            ("Unquoted punctuation character : " + Integer.toString(ch, 16), i);
                    if (inChars) {
                        newChars.append(ch);
                    } else {
                        newExtension.append(ch);
                    }
                    break;
                }
                i++;
            }
            if (newStrength == UNSET)
                return null;
            if (newChars.length() == 0) {
                throw new ParseException
                    ("missing chars (=,;<&): " +
                      pattern.substring(i,
                          (i+10 < pattern.length()) ?
                           i+10 : pattern.length()),
                     i);
            }
            return new PatternEntry(newStrength, newChars, newExtension);
        }
        private StringBuffer newChars = new StringBuffer();
        private StringBuffer newExtension = new StringBuffer();
    }
    static boolean isSpecialChar(char ch) {
        return ((ch == '\u0020') ||
                ((ch <= '\u002F') && (ch >= '\u0022')) ||
                ((ch <= '\u003F') && (ch >= '\u003A')) ||
                ((ch <= '\u0060') && (ch >= '\u005B')) ||
                ((ch <= '\u007E') && (ch >= '\u007B')));
    }
    static final int RESET = -2;
    static final int UNSET = -1;
    int strength = UNSET;
    String chars = "";
    String extension = "";
}
