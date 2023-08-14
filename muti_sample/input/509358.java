public class NameNormalizer {
    private static final RuleBasedCollator sCompressingCollator;
    static {
        sCompressingCollator = (RuleBasedCollator)Collator.getInstance(null);
        sCompressingCollator.setStrength(Collator.PRIMARY);
        sCompressingCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    }
    private static final RuleBasedCollator sComplexityCollator;
    static {
        sComplexityCollator = (RuleBasedCollator)Collator.getInstance(null);
        sComplexityCollator.setStrength(Collator.TERTIARY);
        sComplexityCollator.setAttribute(CollationAttribute.CASE_FIRST,
                CollationAttribute.VALUE_LOWER_FIRST);
    }
    public static String normalize(String name) {
        return Hex.encodeHex(sCompressingCollator.getSortKey(lettersAndDigitsOnly(name)), true);
    }
    public static int compareComplexity(String name1, String name2) {
        int diff = sComplexityCollator.compare(lettersAndDigitsOnly(name1),
                lettersAndDigitsOnly(name2));
        if (diff != 0) {
            return diff;
        }
        return name1.length() - name2.length();
    }
    private static String lettersAndDigitsOnly(String name) {
        char[] letters = name.toCharArray();
        int length = 0;
        for (int i = 0; i < letters.length; i++) {
            final char c = letters[i];
            if (Character.isLetterOrDigit(c)) {
                letters[length++] = c;
            }
        }
        if (length != letters.length) {
            return new String(letters, 0, length);
        }
        return name;
    }
}
