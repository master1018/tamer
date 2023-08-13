public abstract class NameLookupBuilder {
    private static final int MAX_NAME_TOKENS = 4;
    private final NameSplitter mSplitter;
    private String[][] mNicknameClusters = new String[MAX_NAME_TOKENS][];
    private StringBuilder mStringBuilder = new StringBuilder();
    private String[] mNames = new String[NameSplitter.MAX_TOKENS];
    private static int[] KOREAN_JAUM_CONVERT_MAP = {
        0x1100, 
        0x1101, 
        0x00,   
        0x1102, 
        0x00,   
        0x00,   
        0x1103, 
        0x1104, 
        0x1105, 
        0x00,   
        0x00,   
        0x00,   
        0x00,   
        0x00,   
        0x00,   
        0x00,   
        0x1106, 
        0x1107, 
        0x1108, 
        0x00,   
        0x1109, 
        0x110A, 
        0x110B, 
        0x110C, 
        0x110D, 
        0x110E, 
        0x110F, 
        0x1110, 
        0x1111, 
        0x1112  
    };
    private static int KOREAN_JAUM_CONVERT_MAP_COUNT = 30;
    public NameLookupBuilder(NameSplitter splitter) {
        mSplitter = splitter;
    }
    protected abstract void insertNameLookup(long rawContactId, long dataId, int lookupType,
            String string);
    protected abstract String[] getCommonNicknameClusters(String normalizedName);
    public void insertNameLookup(long rawContactId, long dataId, String name, int fullNameStyle) {
        int tokenCount = mSplitter.tokenize(mNames, name);
        if (tokenCount == 0) {
            return;
        }
        for (int i = 0; i < tokenCount; i++) {
            mNames[i] = normalizeName(mNames[i]);
        }
        boolean tooManyTokens = tokenCount > MAX_NAME_TOKENS;
        if (tooManyTokens) {
            insertNameVariant(rawContactId, dataId, tokenCount, NameLookupType.NAME_EXACT, true);
            Arrays.sort(mNames, 0, tokenCount, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    return s2.length() - s1.length();
                }
            });
            String firstToken = mNames[0];
            for (int i = MAX_NAME_TOKENS; i < tokenCount; i++) {
                mNames[0] = mNames[i];
                insertCollationKey(rawContactId, dataId, MAX_NAME_TOKENS);
            }
            mNames[0] = firstToken;
            tokenCount = MAX_NAME_TOKENS;
        }
        for (int i = 0; i < tokenCount; i++) {
            mNicknameClusters[i] = getCommonNicknameClusters(mNames[i]);
        }
        insertNameVariants(rawContactId, dataId, 0, tokenCount, !tooManyTokens, true);
        insertNicknamePermutations(rawContactId, dataId, 0, tokenCount);
        insertNameShorthandLookup(rawContactId, dataId, name, fullNameStyle);
        insertLocaleBasedSpecificLookup(rawContactId, dataId, name, fullNameStyle);
    }
    private void insertLocaleBasedSpecificLookup(long rawContactId, long dataId, String name,
            int fullNameStyle) {
        if (fullNameStyle == FullNameStyle.KOREAN) {
            insertKoreanNameConsonantsLookup(rawContactId, dataId, name);
        }
    }
    private void insertKoreanNameConsonantsLookup(long rawContactId, long dataId, String name) {
        int position = 0;
        int consonantLength = 0;
        int character;
        final int stringLength = name.length();
        mStringBuilder.setLength(0);
        do {
            character = name.codePointAt(position++);
            if (character == 0x20) {
                continue;
            }
            if ((character < 0x1100) || (character > 0x1112 && character < 0x3131) ||
                    (character > 0x314E && character < 0xAC00) ||
                    (character > 0xD7A3)) {
                break;
            }
            if (character >= 0xAC00) {
                character = 0x1100 + (character - 0xAC00) / 588;
            } else if (character >= 0x3131) {
                if (character - 0x3131 >= KOREAN_JAUM_CONVERT_MAP_COUNT) {
                    break;
                }
                character = KOREAN_JAUM_CONVERT_MAP[character - 0x3131];
                if (character == 0) {
                    break;
                }
            }
            mStringBuilder.appendCodePoint(character);
            consonantLength++;
        } while (position < stringLength);
        if (consonantLength > 1) {
            insertNameLookup(rawContactId, dataId, NameLookupType.NAME_CONSONANTS,
                    normalizeName(mStringBuilder.toString()));
        }
    }
    protected String normalizeName(String name) {
        return NameNormalizer.normalize(name);
    }
    private void insertNameVariants(long rawContactId, long dataId, int fromIndex, int toIndex,
            boolean initiallyExact, boolean buildCollationKey) {
        if (fromIndex == toIndex) {
            insertNameVariant(rawContactId, dataId, toIndex,
                    initiallyExact ? NameLookupType.NAME_EXACT : NameLookupType.NAME_VARIANT,
                    buildCollationKey);
            return;
        }
        String firstToken = mNames[fromIndex];
        for (int i = fromIndex; i < toIndex; i++) {
            mNames[fromIndex] = mNames[i];
            mNames[i] = firstToken;
            insertNameVariants(rawContactId, dataId, fromIndex + 1, toIndex,
                    initiallyExact && i == fromIndex, buildCollationKey);
            mNames[i] = mNames[fromIndex];
            mNames[fromIndex] = firstToken;
        }
    }
    private void insertNameVariant(long rawContactId, long dataId, int tokenCount,
            int lookupType, boolean buildCollationKey) {
        mStringBuilder.setLength(0);
        for (int i = 0; i < tokenCount; i++) {
            if (i != 0) {
                mStringBuilder.append('.');
            }
            mStringBuilder.append(mNames[i]);
        }
        insertNameLookup(rawContactId, dataId, lookupType, mStringBuilder.toString());
        if (buildCollationKey) {
            insertCollationKey(rawContactId, dataId, tokenCount);
        }
    }
    private void insertCollationKey(long rawContactId, long dataId, int tokenCount) {
        mStringBuilder.setLength(0);
        for (int i = 0; i < tokenCount; i++) {
            mStringBuilder.append(mNames[i]);
        }
        insertNameLookup(rawContactId, dataId, NameLookupType.NAME_COLLATION_KEY,
                mStringBuilder.toString());
    }
    private void insertNicknamePermutations(long rawContactId, long dataId, int fromIndex,
            int tokenCount) {
        for (int i = fromIndex; i < tokenCount; i++) {
            String[] clusters = mNicknameClusters[i];
            if (clusters != null) {
                String token = mNames[i];
                for (int j = 0; j < clusters.length; j++) {
                    mNames[i] = clusters[j];
                    insertNameVariants(rawContactId, dataId, 0, tokenCount, false, false);
                    insertNicknamePermutations(rawContactId, dataId, i + 1, tokenCount);
                }
                mNames[i] = token;
            }
        }
    }
    private void insertNameShorthandLookup(long rawContactId, long dataId, String name,
            int fullNameStyle) {
        Iterator<String> it =
                ContactLocaleUtils.getIntance().getNameLookupKeys(name, fullNameStyle);
        if (it != null) {
            while (it.hasNext()) {
                String key = it.next();
                insertNameLookup(rawContactId, dataId, NameLookupType.NAME_SHORTHAND,
                        normalizeName(key));
            }
        }
    }
}
