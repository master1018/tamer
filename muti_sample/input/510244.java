class MorseCodeConverter {
    private static final long SPEED_BASE = 100;
    static final long DOT = SPEED_BASE;
    static final long DASH = SPEED_BASE * 3;
    static final long GAP = SPEED_BASE;
    static final long LETTER_GAP = SPEED_BASE * 3;
    static final long WORD_GAP = SPEED_BASE * 7;
    private static final long[][] LETTERS = new long[][] {
         new long[] { DOT, GAP, DASH },
         new long[] { DASH, GAP, DOT, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DOT, GAP, DASH, GAP, DOT },
         new long[] { DASH, GAP, DOT, GAP, DOT },
         new long[] { DOT },
         new long[] { DOT, GAP, DOT, GAP, DASH, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DOT },
         new long[] { DOT, GAP, DOT, GAP, DOT, GAP, DOT },
         new long[] { DOT, GAP, DOT },
         new long[] { DOT, GAP, DASH, GAP, DASH, GAP, DASH },
         new long[] { DASH, GAP, DOT, GAP, DASH },
         new long[] { DOT, GAP, DASH, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DASH },
         new long[] { DASH, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DASH },
         new long[] { DOT, GAP, DASH, GAP, DASH, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DOT, GAP, DASH },
         new long[] { DOT, GAP, DASH, GAP, DOT },
         new long[] { DOT, GAP, DOT, GAP, DOT },
         new long[] { DASH },
         new long[] { DOT, GAP, DOT, GAP, DASH },
         new long[] { DOT, GAP, DOT, GAP, DASH },
         new long[] { DOT, GAP, DASH, GAP, DASH },
         new long[] { DASH, GAP, DOT, GAP, DOT, GAP, DASH },
         new long[] { DASH, GAP, DOT, GAP, DASH, GAP, DASH },
         new long[] { DASH, GAP, DASH, GAP, DOT, GAP, DOT },
    };
    private static final long[][] NUMBERS = new long[][] {
         new long[] { DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH },
         new long[] { DOT, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH },
         new long[] { DOT, GAP, DOT, GAP, DASH, GAP, DASH, GAP, DASH },
         new long[] { DOT, GAP, DOT, GAP, DOT, GAP, DASH, GAP, DASH },
         new long[] { DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DASH },
         new long[] { DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DOT, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DASH, GAP, DOT, GAP, DOT },
         new long[] { DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DOT },
    };
    private static final long[] ERROR_GAP = new long[] { GAP };
    static long[] pattern(char c) {
        if (c >= 'A' && c <= 'Z') {
            return LETTERS[c - 'A'];
        }
        if (c >= 'a' && c <= 'z') {
            return LETTERS[c - 'a'];
        }
        else if (c >= '0' && c <= '9') {
            return NUMBERS[c - '0'];
        }
        else {
            return ERROR_GAP;
        }
    }
    static long[] pattern(String str) {
        boolean lastWasWhitespace;
        int strlen = str.length();
        int len = 1;
        lastWasWhitespace = true;
        for (int i=0; i<strlen; i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    len++;
                    lastWasWhitespace = true;
                }
            } else {
                if (!lastWasWhitespace) {
                    len++;
                }
                lastWasWhitespace = false;
                len += pattern(c).length;
            }
        }
        long[] result = new long[len+1];
        result[0] = 0;
        int pos = 1;
        lastWasWhitespace = true;
        for (int i=0; i<strlen; i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    result[pos] = WORD_GAP;
                    pos++;
                    lastWasWhitespace = true;
                }
            } else {
                if (!lastWasWhitespace) {
                    result[pos] = LETTER_GAP;
                    pos++;
                }
                lastWasWhitespace = false;
                long[] letter = pattern(c);
                System.arraycopy(letter, 0, result, pos, letter.length);
                pos += letter.length;
            }
        }
        return result;
    }
}
