class CaseMapper {
    private static final char LATIN_CAPITAL_I = 'I';
    private static final char LATIN_SMALL_I = 'i';
    private static final char LATIN_CAPITAL_I_WITH_DOT = '\u0130';
    private static final char LATIN_SMALL_DOTLESS_I = '\u0131';
    private static final char COMBINING_DOT_ABOVE = '\u0307';
    private static final char GREEK_CAPITAL_SIGMA = '\u03a3';
    private static final char GREEK_SMALL_FINAL_SIGMA = '\u03c2';
    private CaseMapper() {
    }
    public static String toLowerCase(Locale locale, String s, char[] value, int offset, int count) {
        String languageCode = locale.getLanguage();
        boolean turkishOrAzeri = languageCode.equals("tr") || languageCode.equals("az");
        char[] newValue = null;
        int newCount = 0;
        for (int i = offset, end = offset + count; i < end; ++i) {
            char ch = value[i];
            char newCh = ch;
            if (turkishOrAzeri && ch == LATIN_CAPITAL_I_WITH_DOT) {
                newCh = LATIN_SMALL_I;
            } else if (turkishOrAzeri && ch == LATIN_CAPITAL_I && !followedBy(value, offset, count, i, COMBINING_DOT_ABOVE)) {
                newCh = LATIN_SMALL_DOTLESS_I;
            } else if (turkishOrAzeri && ch == COMBINING_DOT_ABOVE && precededBy(value, offset, count, i, LATIN_CAPITAL_I)) {
                continue; 
            } else if (ch == GREEK_CAPITAL_SIGMA && isFinalSigma(value, offset, count, i)) {
                newCh = GREEK_SMALL_FINAL_SIGMA;
            } else {
                newCh = Character.toLowerCase(ch);
            }
            if (newValue == null && ch != newCh) {
                newValue = new char[count]; 
                newCount = i - offset;
                System.arraycopy(value, offset, newValue, 0, newCount);
            }
            if (newValue != null) {
                newValue[newCount++] = newCh;
            }
        }
        return newValue != null ? new String(0, newCount, newValue) : s;
    }
    private static boolean followedBy(char[] value, int offset, int count, int index, char ch) {
        return index + 1 < offset + count && value[index + 1] == ch;
    }
    private static boolean precededBy(char[] value, int offset, int count, int index, char ch) {
        return index > offset && value[index - 1] == ch;
    }
    private static boolean isFinalSigma(char[] value, int offset, int count, int index) {
        if (index <= offset) {
            return false;
        }
        char previous = value[index - 1];
        if (!(Character.isLowerCase(previous) || Character.isUpperCase(previous) || Character.isTitleCase(previous))) {
            return false;
        }
        if (index + 1 >= offset + count) {
            return true;
        }
        char next = value[index + 1];
        if (Character.isLowerCase(next) || Character.isUpperCase(next) || Character.isTitleCase(next)) {
            return false;
        }
        return true;
    }
}
