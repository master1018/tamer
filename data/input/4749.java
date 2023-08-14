public class StaticStringsHash {
    public String[] strings = null;
    public int[] keys = null;
    public int[][] buckets = null;
    public String method = null;
    public int getKey(String str) {
        switch (keyKind) {
        case LENGTH: return str.length();
        case CHAR_AT: return str.charAt(charAt);
        case HASH_CODE: return str.hashCode();
        }
        throw new Error("Bad keyKind");
    }
    public StaticStringsHash(String[] strings) {
        this.strings = strings;
        length = strings.length;
        tempKeys = new int[length];
        bucketSizes = new int[length];
        setMinStringLength();
        int currentMaxDepth = getKeys(LENGTH);
        int useCharAt = -1;
        boolean useHashCode = false;
        if (currentMaxDepth > 1) {
            int minLength = minStringLength;
            if (length > CHAR_AT_MAX_LINES &&
                length * minLength > CHAR_AT_MAX_CHARS) {
                minLength = length/CHAR_AT_MAX_CHARS;
            }
            charAt = 0;
            for (int i = 0; i < minLength; i++) {
                int charAtDepth = getKeys(CHAR_AT);
                if (charAtDepth < currentMaxDepth) {
                    currentMaxDepth = charAtDepth;
                    useCharAt = i;
                    if (currentMaxDepth == 1) {
                        break;
                    }
                }
                charAt++;
            }
            charAt = useCharAt;
            if (currentMaxDepth > 1) {
                int hashCodeDepth = getKeys(HASH_CODE);
                if (hashCodeDepth < currentMaxDepth-3) {
                    useHashCode = true;
                }
            }
            if (!useHashCode) {
                if (useCharAt >= 0) {
                    getKeys(CHAR_AT);
                } else {
                    getKeys(LENGTH);
                }
            }
        }
        keys = new int[bucketCount];
        System.arraycopy(tempKeys,0,keys,0,bucketCount);
        boolean didSwap;
        do {
            didSwap = false;
            for (int i = 0; i < bucketCount - 1; i++) {
                if (keys[i] > keys[i+1]) {
                    int temp = keys[i];
                    keys[i] = keys[i+1];
                    keys[i+1] = temp;
                    temp = bucketSizes[i];
                    bucketSizes[i] = bucketSizes[i+1];
                    bucketSizes[i+1] = temp;
                    didSwap = true;
                }
            }
        }
        while (didSwap == true);
        int unused = findUnusedKey();
        buckets = new int[bucketCount][];
        for (int i = 0; i < bucketCount; i++) {
            buckets[i] = new int[bucketSizes[i]];
            for (int j = 0; j < bucketSizes[i]; j++) {
                buckets[i][j] = unused;
            }
        }
        for(int i = 0; i < strings.length; i++) {
            int key = getKey(strings[i]);
            for (int j = 0; j < bucketCount; j++) {
                if (keys[j] == key) {
                    int k = 0;
                    while (buckets[j][k] != unused) {
                        k++;
                    }
                    buckets[j][k] = i;
                    break;
                }
            }
        }
    }
    public static void main (String[] args) {
        StaticStringsHash hash = new StaticStringsHash(args);
        System.out.println();
        System.out.println("    public boolean contains(String key) {");
        System.out.println("        switch (key."+hash.method+") {");
        for (int i = 0; i < hash.buckets.length; i++) {
            System.out.println("            case "+hash.keys[i]+": ");
            for (int j = 0; j < hash.buckets[i].length; j++) {
                if (j > 0) {
                    System.out.print("                } else ");
                } else {
                    System.out.print("                ");
                }
                System.out.println("if (key.equals(\""+ hash.strings[hash.buckets[i][j]] +"\")) {");
                System.out.println("                    return true;");
            }
            System.out.println("                }");
        }
        System.out.println("        }");
        System.out.println("        return false;");
        System.out.println("    }");
    }
    private int length;
    private int[] tempKeys;
    private int[] bucketSizes;
    private int bucketCount;
    private int maxDepth;
    private int minStringLength = Integer.MAX_VALUE;
    private int keyKind;
    private int charAt;
    private static final int LENGTH = 0;
    private static final int CHAR_AT = 1;
    private static final int HASH_CODE = 2;
    private static final int CHAR_AT_MAX_LINES = 50;
    private static final int CHAR_AT_MAX_CHARS = 1000;
    private void resetKeys(int keyKind) {
        this.keyKind = keyKind;
        switch (keyKind) {
        case LENGTH: method = "length()"; break;
        case CHAR_AT: method = "charAt("+charAt+")"; break;
        case HASH_CODE: method = "hashCode()"; break;
        }
        maxDepth = 1;
        bucketCount = 0;
        for (int i = 0; i < length; i++) {
            tempKeys[i] = 0;
            bucketSizes[i] = 0;
        }
    }
    private void setMinStringLength() {
        for (int i = 0; i < length; i++) {
            if (strings[i].length() < minStringLength) {
                minStringLength = strings[i].length();
            }
        }
    }
    private int findUnusedKey() {
        int unused = 0;
        int keysLength = keys.length;
        while (true) {
            boolean match = false;
            for (int i = 0; i < keysLength; i++) {
                if (keys[i] == unused) {
                    match = true;
                    break;
                }
            }
            if (match) {
                unused--;
            } else {
                break;
            }
        }
        return unused;
    }
    private int getKeys(int methodKind) {
        resetKeys(methodKind);
        for(int i = 0; i < strings.length; i++) {
            addKey(getKey(strings[i]));
        }
        return maxDepth;
    }
    private void addKey(int key) {
        boolean addIt = true;
        for (int j = 0; j < bucketCount; j++) {
            if (tempKeys[j] == key) {
                addIt = false;
                bucketSizes[j]++;
                if (bucketSizes[j] > maxDepth) {
                    maxDepth = bucketSizes[j];
                }
                break;
            }
        }
        if (addIt) {
            tempKeys[bucketCount] = key;
            bucketSizes[bucketCount] = 1;
            bucketCount++;
        }
    }
}
