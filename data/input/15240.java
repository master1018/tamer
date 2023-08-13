final class ConditionalSpecialCasing {
    final static int FINAL_CASED =              1;
    final static int AFTER_SOFT_DOTTED =        2;
    final static int MORE_ABOVE =               3;
    final static int AFTER_I =                  4;
    final static int NOT_BEFORE_DOT =           5;
    final static int COMBINING_CLASS_ABOVE = 230;
    static Entry[] entry = {
        new Entry(0x03A3, new char[]{0x03C2}, new char[]{0x03A3}, null, FINAL_CASED), 
        new Entry(0x0307, new char[]{0x0307}, new char[]{}, "lt",  AFTER_SOFT_DOTTED), 
        new Entry(0x0049, new char[]{0x0069, 0x0307}, new char[]{0x0049}, "lt", MORE_ABOVE), 
        new Entry(0x004A, new char[]{0x006A, 0x0307}, new char[]{0x004A}, "lt", MORE_ABOVE), 
        new Entry(0x012E, new char[]{0x012F, 0x0307}, new char[]{0x012E}, "lt", MORE_ABOVE), 
        new Entry(0x00CC, new char[]{0x0069, 0x0307, 0x0300}, new char[]{0x00CC}, "lt", 0), 
        new Entry(0x00CD, new char[]{0x0069, 0x0307, 0x0301}, new char[]{0x00CD}, "lt", 0), 
        new Entry(0x0128, new char[]{0x0069, 0x0307, 0x0303}, new char[]{0x0128}, "lt", 0), 
        new Entry(0x0130, new char[]{0x0069, 0x0307}, new char[]{0x0130}, "lt", 0), 
        new Entry(0x0307, new char[]{}, new char[]{0x0307}, "tr", AFTER_I), 
        new Entry(0x0307, new char[]{}, new char[]{0x0307}, "az", AFTER_I), 
        new Entry(0x0049, new char[]{0x0131}, new char[]{0x0049}, "tr", NOT_BEFORE_DOT), 
        new Entry(0x0049, new char[]{0x0131}, new char[]{0x0049}, "az", NOT_BEFORE_DOT), 
        new Entry(0x0069, new char[]{0x0069}, new char[]{0x0130}, "tr", 0), 
        new Entry(0x0069, new char[]{0x0069}, new char[]{0x0130}, "az", 0), 
        new Entry(0x0130, new char[]{0x0069, 0x0307}, new char[]{0x0130}, "en", 0), 
    };
    static Hashtable entryTable = new Hashtable();
    static {
        for (int i = 0; i < entry.length; i ++) {
            Entry cur = entry[i];
            Integer cp = new Integer(cur.getCodePoint());
            HashSet set = (HashSet)entryTable.get(cp);
            if (set == null) {
                set = new HashSet();
            }
            set.add(cur);
            entryTable.put(cp, set);
        }
    }
    static int toLowerCaseEx(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, true);
        if (result != null) {
            if (result.length == 1) {
                return result[0];
            } else {
                return Character.ERROR;
            }
        } else {
            return Character.toLowerCase(src.codePointAt(index));
        }
    }
    static int toUpperCaseEx(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, false);
        if (result != null) {
            if (result.length == 1) {
                return result[0];
            } else {
                return Character.ERROR;
            }
        } else {
            return Character.toUpperCaseEx(src.codePointAt(index));
        }
    }
    static char[] toLowerCaseCharArray(String src, int index, Locale locale) {
        return lookUpTable(src, index, locale, true);
    }
    static char[] toUpperCaseCharArray(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, false);
        if (result != null) {
            return result;
        } else {
            return Character.toUpperCaseCharArray(src.codePointAt(index));
        }
    }
    private static char[] lookUpTable(String src, int index, Locale locale, boolean bLowerCasing) {
        HashSet set = (HashSet)entryTable.get(new Integer(src.codePointAt(index)));
        if (set != null) {
            Iterator iter = set.iterator();
            String currentLang = locale.getLanguage();
            while (iter.hasNext()) {
                Entry entry = (Entry)iter.next();
                String conditionLang= entry.getLanguage();
                if (((conditionLang == null) || (conditionLang.equals(currentLang))) &&
                        isConditionMet(src, index, locale, entry.getCondition())) {
                    return (bLowerCasing ? entry.getLowerCase() : entry.getUpperCase());
                }
            }
        }
        return null;
    }
    private static boolean isConditionMet(String src, int index, Locale locale, int condition) {
        switch (condition) {
        case FINAL_CASED:
            return isFinalCased(src, index, locale);
        case AFTER_SOFT_DOTTED:
            return isAfterSoftDotted(src, index);
        case MORE_ABOVE:
            return isMoreAbove(src, index);
        case AFTER_I:
            return isAfterI(src, index);
        case NOT_BEFORE_DOT:
            return !isBeforeDot(src, index);
        default:
            return true;
        }
    }
    private static boolean isFinalCased(String src, int index, Locale locale) {
        BreakIterator wordBoundary = BreakIterator.getWordInstance(locale);
        wordBoundary.setText(src);
        int ch;
        for (int i = index; (i >= 0) && !wordBoundary.isBoundary(i);
                i -= Character.charCount(ch)) {
            ch = src.codePointBefore(i);
            if (isCased(ch)) {
                int len = src.length();
                for (i = index + Character.charCount(src.codePointAt(index));
                        (i < len) && !wordBoundary.isBoundary(i);
                        i += Character.charCount(ch)) {
                    ch = src.codePointAt(i);
                    if (isCased(ch)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    private static boolean isAfterI(String src, int index) {
        int ch;
        int cc;
        for (int i = index; i > 0; i -= Character.charCount(ch)) {
            ch = src.codePointBefore(i);
            if (ch == 'I') {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }
        return false;
    }
    private static boolean isAfterSoftDotted(String src, int index) {
        int ch;
        int cc;
        for (int i = index; i > 0; i -= Character.charCount(ch)) {
            ch = src.codePointBefore(i);
            if (isSoftDotted(ch)) {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }
        return false;
    }
    private static boolean isMoreAbove(String src, int index) {
        int ch;
        int cc;
        int len = src.length();
        for (int i = index + Character.charCount(src.codePointAt(index));
                i < len; i += Character.charCount(ch)) {
            ch = src.codePointAt(i);
            cc = Normalizer.getCombiningClass(ch);
            if (cc == COMBINING_CLASS_ABOVE) {
                return true;
            } else if (cc == 0) {
                return false;
            }
        }
        return false;
    }
    private static boolean isBeforeDot(String src, int index) {
        int ch;
        int cc;
        int len = src.length();
        for (int i = index + Character.charCount(src.codePointAt(index));
                i < len; i += Character.charCount(ch)) {
            ch = src.codePointAt(i);
            if (ch == '\u0307') {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }
        return false;
    }
    private static boolean isCased(int ch) {
        int type = Character.getType(ch);
        if (type == Character.LOWERCASE_LETTER ||
                type == Character.UPPERCASE_LETTER ||
                type == Character.TITLECASE_LETTER) {
            return true;
        } else {
            if ((ch >= 0x02B0) && (ch <= 0x02B8)) {
                return true;
            } else if ((ch >= 0x02C0) && (ch <= 0x02C1)) {
                return true;
            } else if ((ch >= 0x02E0) && (ch <= 0x02E4)) {
                return true;
            } else if (ch == 0x0345) {
                return true;
            } else if (ch == 0x037A) {
                return true;
            } else if ((ch >= 0x1D2C) && (ch <= 0x1D61)) {
                return true;
            } else if ((ch >= 0x2160) && (ch <= 0x217F)) {
                return true;
            } else if ((ch >= 0x24B6) && (ch <= 0x24E9)) {
                return true;
            } else {
                return false;
            }
        }
    }
    private static boolean isSoftDotted(int ch) {
        switch (ch) {
        case 0x0069: 
        case 0x006A: 
        case 0x012F: 
        case 0x0268: 
        case 0x0456: 
        case 0x0458: 
        case 0x1D62: 
        case 0x1E2D: 
        case 0x1ECB: 
        case 0x2071: 
            return true;
        default:
            return false;
        }
    }
    static class Entry {
        int ch;
        char [] lower;
        char [] upper;
        String lang;
        int condition;
        Entry(int ch, char[] lower, char[] upper, String lang, int condition) {
            this.ch = ch;
            this.lower = lower;
            this.upper = upper;
            this.lang = lang;
            this.condition = condition;
        }
        int getCodePoint() {
            return ch;
        }
        char[] getLowerCase() {
            return lower;
        }
        char[] getUpperCase() {
            return upper;
        }
        String getLanguage() {
            return lang;
        }
        int getCondition() {
            return condition;
        }
    }
}
