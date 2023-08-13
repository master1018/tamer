public class NameSplitter {
    public static final int MAX_TOKENS = 10;
    private static final String JAPANESE_LANGUAGE = Locale.JAPANESE.getLanguage().toLowerCase();
    private static final String KOREAN_LANGUAGE = Locale.KOREAN.getLanguage().toLowerCase();
    private static final String CHINESE_LANGUAGE = Locale.CHINESE.getLanguage().toLowerCase();
    private final HashSet<String> mPrefixesSet;
    private final HashSet<String> mSuffixesSet;
    private final int mMaxSuffixLength;
    private final HashSet<String> mLastNamePrefixesSet;
    private final HashSet<String> mConjuctions;
    private final Locale mLocale;
    private final String mLanguage;
    public static class Name {
        public String prefix;
        public String givenNames;
        public String middleName;
        public String familyName;
        public String suffix;
        public int fullNameStyle;
        public String phoneticFamilyName;
        public String phoneticMiddleName;
        public String phoneticGivenName;
        public int phoneticNameStyle;
        public Name() {
        }
        public Name(String prefix, String givenNames, String middleName, String familyName,
                String suffix) {
            this.prefix = prefix;
            this.givenNames = givenNames;
            this.middleName = middleName;
            this.familyName = familyName;
            this.suffix = suffix;
        }
        public String getPrefix() {
            return prefix;
        }
        public String getGivenNames() {
            return givenNames;
        }
        public String getMiddleName() {
            return middleName;
        }
        public String getFamilyName() {
            return familyName;
        }
        public String getSuffix() {
            return suffix;
        }
        public int getFullNameStyle() {
            return fullNameStyle;
        }
        public String getPhoneticFamilyName() {
            return phoneticFamilyName;
        }
        public String getPhoneticMiddleName() {
            return phoneticMiddleName;
        }
        public String getPhoneticGivenName() {
            return phoneticGivenName;
        }
        public int getPhoneticNameStyle() {
            return phoneticNameStyle;
        }
        public void fromValues(ContentValues values) {
            prefix = values.getAsString(StructuredName.PREFIX);
            givenNames = values.getAsString(StructuredName.GIVEN_NAME);
            middleName = values.getAsString(StructuredName.MIDDLE_NAME);
            familyName = values.getAsString(StructuredName.FAMILY_NAME);
            suffix = values.getAsString(StructuredName.SUFFIX);
            Integer integer = values.getAsInteger(StructuredName.FULL_NAME_STYLE);
            fullNameStyle = integer == null ? FullNameStyle.UNDEFINED : integer;
            phoneticFamilyName = values.getAsString(StructuredName.PHONETIC_FAMILY_NAME);
            phoneticMiddleName = values.getAsString(StructuredName.PHONETIC_MIDDLE_NAME);
            phoneticGivenName = values.getAsString(StructuredName.PHONETIC_GIVEN_NAME);
            integer = values.getAsInteger(StructuredName.PHONETIC_NAME_STYLE);
            phoneticNameStyle = integer == null ? PhoneticNameStyle.UNDEFINED : integer;
        }
        public void toValues(ContentValues values) {
            putValueIfPresent(values, StructuredName.PREFIX, prefix);
            putValueIfPresent(values, StructuredName.GIVEN_NAME, givenNames);
            putValueIfPresent(values, StructuredName.MIDDLE_NAME, middleName);
            putValueIfPresent(values, StructuredName.FAMILY_NAME, familyName);
            putValueIfPresent(values, StructuredName.SUFFIX, suffix);
            values.put(StructuredName.FULL_NAME_STYLE, fullNameStyle);
            putValueIfPresent(values, StructuredName.PHONETIC_FAMILY_NAME, phoneticFamilyName);
            putValueIfPresent(values, StructuredName.PHONETIC_MIDDLE_NAME, phoneticMiddleName);
            putValueIfPresent(values, StructuredName.PHONETIC_GIVEN_NAME, phoneticGivenName);
            values.put(StructuredName.PHONETIC_NAME_STYLE, phoneticNameStyle);
        }
        private void putValueIfPresent(ContentValues values, String name, String value) {
            if (value != null) {
                values.put(name, value);
            }
        }
        public void clear() {
            prefix = null;
            givenNames = null;
            middleName = null;
            familyName = null;
            suffix = null;
            fullNameStyle = FullNameStyle.UNDEFINED;
            phoneticFamilyName = null;
            phoneticMiddleName = null;
            phoneticGivenName = null;
            phoneticNameStyle = PhoneticNameStyle.UNDEFINED;
        }
        public boolean isEmpty() {
            return TextUtils.isEmpty(givenNames)
                    && TextUtils.isEmpty(middleName)
                    && TextUtils.isEmpty(familyName)
                    && TextUtils.isEmpty(suffix)
                    && TextUtils.isEmpty(phoneticFamilyName)
                    && TextUtils.isEmpty(phoneticMiddleName)
                    && TextUtils.isEmpty(phoneticGivenName);
        }
        @Override
        public String toString() {
            return "[given: " + givenNames + " middle: " + middleName + " family: " + familyName
                    + " ph/given: " + phoneticGivenName + " ph/middle: " + phoneticMiddleName
                    + " ph/family: " + phoneticFamilyName + "]";
        }
    }
    private static class NameTokenizer extends StringTokenizer {
        private final String[] mTokens;
        private int mDotBitmask;
        private int mCommaBitmask;
        private int mStartPointer;
        private int mEndPointer;
        public NameTokenizer(String fullName) {
            super(fullName, " .,", true);
            mTokens = new String[MAX_TOKENS];
            while (hasMoreTokens() && mEndPointer < MAX_TOKENS) {
                final String token = nextToken();
                if (token.length() > 0) {
                    final char c = token.charAt(0);
                    if (c == ' ') {
                        continue;
                    }
                }
                if (mEndPointer > 0 && token.charAt(0) == '.') {
                    mDotBitmask |= (1 << (mEndPointer - 1));
                } else if (mEndPointer > 0 && token.charAt(0) == ',') {
                    mCommaBitmask |= (1 << (mEndPointer - 1));
                } else {
                    mTokens[mEndPointer] = token;
                    mEndPointer++;
                }
            }
        }
        public boolean hasDot(int index) {
            return (mDotBitmask & (1 << index)) != 0;
        }
        public boolean hasComma(int index) {
            return (mCommaBitmask & (1 << index)) != 0;
        }
    }
    public NameSplitter(String commonPrefixes, String commonLastNamePrefixes,
            String commonSuffixes, String commonConjunctions, Locale locale) {
        mPrefixesSet = convertToSet(commonPrefixes);
        mLastNamePrefixesSet = convertToSet(commonLastNamePrefixes);
        mSuffixesSet = convertToSet(commonSuffixes);
        mConjuctions = convertToSet(commonConjunctions);
        mLocale = locale != null ? locale : Locale.getDefault();
        mLanguage = mLocale.getLanguage().toLowerCase();
        int maxLength = 0;
        for (String suffix : mSuffixesSet) {
            if (suffix.length() > maxLength) {
                maxLength = suffix.length();
            }
        }
        mMaxSuffixLength = maxLength;
    }
    private static HashSet<String> convertToSet(String strings) {
        HashSet<String> set = new HashSet<String>();
        if (strings != null) {
            String[] split = strings.split(",");
            for (int i = 0; i < split.length; i++) {
                set.add(split[i].trim().toUpperCase());
            }
        }
        return set;
    }
    public int tokenize(String[] tokens, String fullName) {
        if (fullName == null) {
            return 0;
        }
        NameTokenizer tokenizer = new NameTokenizer(fullName);
        if (tokenizer.mStartPointer == tokenizer.mEndPointer) {
            return 0;
        }
        String firstToken = tokenizer.mTokens[tokenizer.mStartPointer];
        if (mPrefixesSet.contains(firstToken.toUpperCase())) {
           tokenizer.mStartPointer++;
        }
        int count = 0;
        for (int i = tokenizer.mStartPointer; i < tokenizer.mEndPointer; i++) {
            tokens[count++] = tokenizer.mTokens[i];
        }
        return count;
    }
    public void split(Name name, String fullName) {
        if (fullName == null) {
            return;
        }
        int fullNameStyle = guessFullNameStyle(fullName);
        if (fullNameStyle == FullNameStyle.CJK) {
            fullNameStyle = getAdjustedFullNameStyle(fullNameStyle);
        }
        name.fullNameStyle = fullNameStyle;
        switch (fullNameStyle) {
            case FullNameStyle.CHINESE:
                splitChineseName(name, fullName);
                break;
            case FullNameStyle.JAPANESE:
            case FullNameStyle.KOREAN:
                splitJapaneseOrKoreanName(name, fullName);
                break;
            default:
                splitWesternName(name, fullName);
        }
    }
    private void splitWesternName(Name name, String fullName) {
        NameTokenizer tokens = new NameTokenizer(fullName);
        parsePrefix(name, tokens);
        if (tokens.mEndPointer > 2) {
            parseSuffix(name, tokens);
        }
        if (name.prefix == null && tokens.mEndPointer - tokens.mStartPointer == 1) {
            name.givenNames = tokens.mTokens[tokens.mStartPointer];
        } else {
            parseLastName(name, tokens);
            parseMiddleName(name, tokens);
            parseGivenNames(name, tokens);
        }
    }
    private void splitChineseName(Name name, String fullName) {
        StringTokenizer tokenizer = new StringTokenizer(fullName);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (name.givenNames == null) {
                name.givenNames = token;
            } else if (name.familyName == null) {
                name.familyName = name.givenNames;
                name.givenNames = token;
            } else if (name.middleName == null) {
                name.middleName = name.givenNames;
                name.givenNames = token;
            } else {
                name.middleName = name.middleName + name.givenNames;
                name.givenNames = token;
            }
        }
        if (name.givenNames != null && name.familyName == null && name.middleName == null) {
            int length = fullName.length();
            if (length == 2) {
                name.familyName = fullName.substring(0, 1);
                name.givenNames = fullName.substring(1);
            } else if (length == 3) {
                name.familyName = fullName.substring(0, 1);
                name.middleName = fullName.substring(1, 2);
                name.givenNames = fullName.substring(2);
            } else if (length == 4) {
                name.familyName = fullName.substring(0, 2);
                name.middleName = fullName.substring(2, 3);
                name.givenNames = fullName.substring(3);
            }
        }
    }
    private void splitJapaneseOrKoreanName(Name name, String fullName) {
        StringTokenizer tokenizer = new StringTokenizer(fullName);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (name.givenNames == null) {
                name.givenNames = token;
            } else if (name.familyName == null) {
                name.familyName = name.givenNames;
                name.givenNames = token;
            } else {
                name.givenNames += " " + token;
            }
        }
    }
    public String join(Name name, boolean givenNameFirst) {
        switch (name.fullNameStyle) {
            case FullNameStyle.CJK:
            case FullNameStyle.CHINESE:
            case FullNameStyle.KOREAN:
                return join(name.familyName, name.middleName, name.givenNames, name.suffix,
                        false, false, false);
            case FullNameStyle.JAPANESE:
                return join(name.familyName, name.middleName, name.givenNames, name.suffix,
                        true, false, false);
            default:
                if (givenNameFirst) {
                    return join(name.givenNames, name.middleName, name.familyName, name.suffix,
                            true, false, true);
                } else {
                    return join(name.familyName, name.givenNames, name.middleName, name.suffix,
                            true, true, true);
                }
        }
    }
    public String joinPhoneticName(Name name) {
        return join(name.phoneticFamilyName, name.phoneticMiddleName,
                name.phoneticGivenName, null, true, false, false);
    }
    private String join(String part1, String part2, String part3, String suffix,
            boolean useSpace, boolean useCommaAfterPart1, boolean useCommaAfterPart3) {
        boolean hasPart1 = !TextUtils.isEmpty(part1);
        boolean hasPart2 = !TextUtils.isEmpty(part2);
        boolean hasPart3 = !TextUtils.isEmpty(part3);
        boolean hasSuffix = !TextUtils.isEmpty(suffix);
        boolean isSingleWord = true;
        String singleWord = null;
        if (hasPart1) {
            singleWord = part1;
        }
        if (hasPart2) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = part2;
            }
        }
        if (hasPart3) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = part3;
            }
        }
        if (hasSuffix) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = normalizedSuffix(suffix);
            }
        }
        if (isSingleWord) {
            return singleWord;
        }
        StringBuilder sb = new StringBuilder();
        if (hasPart1) {
            sb.append(part1);
        }
        if (hasPart2) {
            if (hasPart1) {
                if (useCommaAfterPart1) {
                    sb.append(',');
                }
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(part2);
        }
        if (hasPart3) {
            if (hasPart1 || hasPart2) {
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(part3);
        }
        if (hasSuffix) {
            if (hasPart1 || hasPart2 || hasPart3) {
                if (useCommaAfterPart3) {
                    sb.append(',');
                }
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(normalizedSuffix(suffix));
        }
        return sb.toString();
    }
    private String normalizedSuffix(String suffix) {
        int length = suffix.length();
        if (length == 0 || suffix.charAt(length - 1) == '.') {
            return suffix;
        }
        String withDot = suffix + '.';
        if (mSuffixesSet.contains(withDot.toUpperCase())) {
            return withDot;
        } else {
            return suffix;
        }
    }
    public int getAdjustedFullNameStyle(int nameStyle) {
        if (nameStyle == FullNameStyle.UNDEFINED) {
            if (JAPANESE_LANGUAGE.equals(mLanguage)) {
                return FullNameStyle.JAPANESE;
            } else if (KOREAN_LANGUAGE.equals(mLanguage)) {
                return FullNameStyle.KOREAN;
            } else if (CHINESE_LANGUAGE.equals(mLanguage)) {
                return FullNameStyle.CHINESE;
            } else {
                return FullNameStyle.WESTERN;
            }
        } else if (nameStyle == FullNameStyle.CJK) {
            if (JAPANESE_LANGUAGE.equals(mLanguage)) {
                return FullNameStyle.JAPANESE;
            } else if (KOREAN_LANGUAGE.equals(mLanguage)) {
                return FullNameStyle.KOREAN;
            } else {
                return FullNameStyle.CHINESE;
            }
        }
        return nameStyle;
    }
    private void parsePrefix(Name name, NameTokenizer tokens) {
        if (tokens.mStartPointer == tokens.mEndPointer) {
            return;
        }
        String firstToken = tokens.mTokens[tokens.mStartPointer];
        if (mPrefixesSet.contains(firstToken.toUpperCase())) {
            name.prefix = firstToken;
            tokens.mStartPointer++;
        }
    }
    private void parseSuffix(Name name, NameTokenizer tokens) {
        if (tokens.mStartPointer == tokens.mEndPointer) {
            return;
        }
        String lastToken = tokens.mTokens[tokens.mEndPointer - 1];
        if (lastToken.length() > mMaxSuffixLength) {
            return;
        }
        String normalized = lastToken.toUpperCase();
        if (mSuffixesSet.contains(normalized)) {
            name.suffix = lastToken;
            tokens.mEndPointer--;
            return;
        }
        if (tokens.hasDot(tokens.mEndPointer - 1)) {
            lastToken += '.';
        }
        normalized += ".";
        int pos = tokens.mEndPointer - 1;
        while (normalized.length() <= mMaxSuffixLength) {
            if (mSuffixesSet.contains(normalized)) {
                name.suffix = lastToken;
                tokens.mEndPointer = pos;
                return;
            }
            if (pos == tokens.mStartPointer) {
                break;
            }
            pos--;
            if (tokens.hasDot(pos)) {
                lastToken = tokens.mTokens[pos] + "." + lastToken;
            } else {
                lastToken = tokens.mTokens[pos] + " " + lastToken;
            }
            normalized = tokens.mTokens[pos].toUpperCase() + "." + normalized;
        }
    }
    private void parseLastName(Name name, NameTokenizer tokens) {
        if (tokens.mStartPointer == tokens.mEndPointer) {
            return;
        }
        if (tokens.hasComma(tokens.mStartPointer)) {
           name.familyName = tokens.mTokens[tokens.mStartPointer];
           tokens.mStartPointer++;
           return;
        }
        if (tokens.mStartPointer + 1 < tokens.mEndPointer
                && tokens.hasComma(tokens.mStartPointer + 1)
                && isFamilyNamePrefix(tokens.mTokens[tokens.mStartPointer])) {
            String familyNamePrefix = tokens.mTokens[tokens.mStartPointer];
            if (tokens.hasDot(tokens.mStartPointer)) {
                familyNamePrefix += '.';
            }
            name.familyName = familyNamePrefix + " " + tokens.mTokens[tokens.mStartPointer + 1];
            tokens.mStartPointer += 2;
            return;
        }
        name.familyName = tokens.mTokens[tokens.mEndPointer - 1];
        tokens.mEndPointer--;
        if ((tokens.mEndPointer - tokens.mStartPointer) > 0) {
            String lastNamePrefix = tokens.mTokens[tokens.mEndPointer - 1];
            if (isFamilyNamePrefix(lastNamePrefix)) {
                if (tokens.hasDot(tokens.mEndPointer - 1)) {
                    lastNamePrefix += '.';
                }
                name.familyName = lastNamePrefix + " " + name.familyName;
                tokens.mEndPointer--;
            }
        }
    }
    private boolean isFamilyNamePrefix(String word) {
        final String normalized = word.toUpperCase();
        return mLastNamePrefixesSet.contains(normalized)
                || mLastNamePrefixesSet.contains(normalized + ".");
    }
    private void parseMiddleName(Name name, NameTokenizer tokens) {
        if (tokens.mStartPointer == tokens.mEndPointer) {
            return;
        }
        if ((tokens.mEndPointer - tokens.mStartPointer) > 1) {
            if ((tokens.mEndPointer - tokens.mStartPointer) == 2
                    || !mConjuctions.contains(tokens.mTokens[tokens.mEndPointer - 2].
                            toUpperCase())) {
                name.middleName = tokens.mTokens[tokens.mEndPointer - 1];
                if (tokens.hasDot(tokens.mEndPointer - 1)) {
                    name.middleName += '.';
                }
                tokens.mEndPointer--;
            }
        }
    }
    private void parseGivenNames(Name name, NameTokenizer tokens) {
        if (tokens.mStartPointer == tokens.mEndPointer) {
            return;
        }
        if ((tokens.mEndPointer - tokens.mStartPointer) == 1) {
            name.givenNames = tokens.mTokens[tokens.mStartPointer];
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = tokens.mStartPointer; i < tokens.mEndPointer; i++) {
                if (i != tokens.mStartPointer) {
                    sb.append(' ');
                }
                sb.append(tokens.mTokens[i]);
                if (tokens.hasDot(i)) {
                    sb.append('.');
                }
            }
            name.givenNames = sb.toString();
        }
    }
    public void guessNameStyle(Name name) {
        guessFullNameStyle(name);
        guessPhoneticNameStyle(name);
        name.fullNameStyle = getAdjustedNameStyleBasedOnPhoneticNameStyle(name.fullNameStyle,
                name.phoneticNameStyle);
    }
    public int getAdjustedNameStyleBasedOnPhoneticNameStyle(int nameStyle, int phoneticNameStyle) {
        if (phoneticNameStyle != PhoneticNameStyle.UNDEFINED) {
            if (nameStyle == FullNameStyle.UNDEFINED || nameStyle == FullNameStyle.CJK) {
                if (phoneticNameStyle == PhoneticNameStyle.JAPANESE) {
                    return FullNameStyle.JAPANESE;
                } else if (phoneticNameStyle == PhoneticNameStyle.KOREAN) {
                    return FullNameStyle.KOREAN;
                }
                if (nameStyle == FullNameStyle.CJK && phoneticNameStyle == PhoneticNameStyle.PINYIN) {
                    return FullNameStyle.CHINESE;
                }
            }
        }
        return nameStyle;
    }
    private void guessFullNameStyle(NameSplitter.Name name) {
        if (name.fullNameStyle != FullNameStyle.UNDEFINED) {
            return;
        }
        int bestGuess = guessFullNameStyle(name.givenNames);
        if (bestGuess != FullNameStyle.UNDEFINED && bestGuess != FullNameStyle.CJK
                && bestGuess != FullNameStyle.WESTERN) {
            name.fullNameStyle = bestGuess;
            return;
        }
        int guess = guessFullNameStyle(name.familyName);
        if (guess != FullNameStyle.UNDEFINED) {
            if (guess != FullNameStyle.CJK && guess != FullNameStyle.WESTERN) {
                name.fullNameStyle = guess;
                return;
            }
            bestGuess = guess;
        }
        guess = guessFullNameStyle(name.middleName);
        if (guess != FullNameStyle.UNDEFINED) {
            if (guess != FullNameStyle.CJK && guess != FullNameStyle.WESTERN) {
                name.fullNameStyle = guess;
                return;
            }
            bestGuess = guess;
        }
        name.fullNameStyle = bestGuess;
    }
    public int guessFullNameStyle(String name) {
        if (name == null) {
            return FullNameStyle.UNDEFINED;
        }
        int nameStyle = FullNameStyle.UNDEFINED;
        int length = name.length();
        int offset = 0;
        while (offset < length) {
            int codePoint = Character.codePointAt(name, offset);
            if (Character.isLetter(codePoint)) {
                UnicodeBlock unicodeBlock = UnicodeBlock.of(codePoint);
                if (!isLatinUnicodeBlock(unicodeBlock)) {
                    if (isCJKUnicodeBlock(unicodeBlock)) {
                        return guessCJKNameStyle(name, offset + Character.charCount(codePoint));
                    }
                    if (isJapanesePhoneticUnicodeBlock(unicodeBlock)) {
                        return FullNameStyle.JAPANESE;
                    }
                    if (isKoreanUnicodeBlock(unicodeBlock)) {
                        return FullNameStyle.KOREAN;
                    }
                }
                nameStyle = FullNameStyle.WESTERN;
            }
            offset += Character.charCount(codePoint);
        }
        return nameStyle;
    }
    private int guessCJKNameStyle(String name, int offset) {
        int length = name.length();
        while (offset < length) {
            int codePoint = Character.codePointAt(name, offset);
            if (Character.isLetter(codePoint)) {
                UnicodeBlock unicodeBlock = UnicodeBlock.of(codePoint);
                if (isJapanesePhoneticUnicodeBlock(unicodeBlock)) {
                    return FullNameStyle.JAPANESE;
                }
                if (isKoreanUnicodeBlock(unicodeBlock)) {
                    return FullNameStyle.KOREAN;
                }
            }
            offset += Character.charCount(codePoint);
        }
        return FullNameStyle.CJK;
    }
    private void guessPhoneticNameStyle(NameSplitter.Name name) {
        if (name.phoneticNameStyle != PhoneticNameStyle.UNDEFINED) {
            return;
        }
        int bestGuess = guessPhoneticNameStyle(name.phoneticFamilyName);
        if (bestGuess != FullNameStyle.UNDEFINED && bestGuess != FullNameStyle.CJK) {
            name.phoneticNameStyle = bestGuess;
            return;
        }
        int guess = guessPhoneticNameStyle(name.phoneticGivenName);
        if (guess != FullNameStyle.UNDEFINED) {
            if (guess != FullNameStyle.CJK) {
                name.phoneticNameStyle = guess;
                return;
            }
            bestGuess = guess;
        }
        guess = guessPhoneticNameStyle(name.phoneticMiddleName);
        if (guess != FullNameStyle.UNDEFINED) {
            if (guess != FullNameStyle.CJK) {
                name.phoneticNameStyle = guess;
                return;
            }
            bestGuess = guess;
        }
    }
    public int guessPhoneticNameStyle(String name) {
        if (name == null) {
            return PhoneticNameStyle.UNDEFINED;
        }
        int nameStyle = PhoneticNameStyle.UNDEFINED;
        int length = name.length();
        int offset = 0;
        while (offset < length) {
            int codePoint = Character.codePointAt(name, offset);
            if (Character.isLetter(codePoint)) {
                UnicodeBlock unicodeBlock = UnicodeBlock.of(codePoint);
                if (isJapanesePhoneticUnicodeBlock(unicodeBlock)) {
                    return PhoneticNameStyle.JAPANESE;
                }
                if (isKoreanUnicodeBlock(unicodeBlock)) {
                    return PhoneticNameStyle.KOREAN;
                }
                if (isLatinUnicodeBlock(unicodeBlock)) {
                    return PhoneticNameStyle.PINYIN;
                }
            }
            offset += Character.charCount(codePoint);
        }
        return nameStyle;
    }
    private static boolean isLatinUnicodeBlock(UnicodeBlock unicodeBlock) {
        return unicodeBlock == UnicodeBlock.BASIC_LATIN ||
                unicodeBlock == UnicodeBlock.LATIN_1_SUPPLEMENT ||
                unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A ||
                unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B ||
                unicodeBlock == UnicodeBlock.LATIN_EXTENDED_ADDITIONAL;
    }
    private static boolean isCJKUnicodeBlock(UnicodeBlock block) {
        return block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == UnicodeBlock.CJK_RADICALS_SUPPLEMENT
                || block == UnicodeBlock.CJK_COMPATIBILITY
                || block == UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || block == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT;
    }
    private static boolean isKoreanUnicodeBlock(UnicodeBlock unicodeBlock) {
        return unicodeBlock == UnicodeBlock.HANGUL_SYLLABLES ||
                unicodeBlock == UnicodeBlock.HANGUL_JAMO ||
                unicodeBlock == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
    }
    private static boolean isJapanesePhoneticUnicodeBlock(UnicodeBlock unicodeBlock) {
        return unicodeBlock == UnicodeBlock.KATAKANA ||
                unicodeBlock == UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS ||
                unicodeBlock == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                unicodeBlock == UnicodeBlock.HIRAGANA;
    }
}
