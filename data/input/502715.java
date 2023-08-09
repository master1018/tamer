public class LevenshteinSuggestionFormatter extends SuggestionFormatter {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.LevenshteinSuggestionFormatter";
    public LevenshteinSuggestionFormatter(TextAppearanceFactory spanFactory) {
        super(spanFactory);
    }
    @Override
    public Spanned formatSuggestion(String query, String suggestion) {
        if (DBG) Log.d(TAG, "formatSuggestion('" + query + "', '" + suggestion + "')");
        query = normalizeQuery(query);
        final Token[] queryTokens = tokenize(query);
        final Token[] suggestionTokens = tokenize(suggestion);
        final int[] matches = findMatches(queryTokens, suggestionTokens);
        if (DBG){
            Log.d(TAG, "source = " + queryTokens);
            Log.d(TAG, "target = " + suggestionTokens);
            Log.d(TAG, "matches = " + matches);
        }
        final SpannableString str = new SpannableString(suggestion);
        final int matchesLen = matches.length;
        for (int i = 0; i < matchesLen; ++i) {
            final Token t = suggestionTokens[i];
            int sourceLen = 0;
            int thisMatch = matches[i];
            if (thisMatch >= 0) {
                sourceLen = queryTokens[thisMatch].length();
            }
            applySuggestedTextStyle(str, t.mStart + sourceLen, t.mEnd);
            applyQueryTextStyle(str, t.mStart, t.mStart + sourceLen);
        }
        return str;
    }
    private String normalizeQuery(String query) {
        return query.toLowerCase();
    }
    @VisibleForTesting
    int[] findMatches(Token[] source, Token[] target) {
        final LevenshteinDistance table = new LevenshteinDistance(source, target);
        table.calculate();
        final int targetLen = target.length;
        final int[] result = new int[targetLen];
        LevenshteinDistance.EditOperation[] ops = table.getTargetOperations();
        for (int i = 0; i < targetLen; ++i) {
            if (ops[i].getType() == LevenshteinDistance.EDIT_UNCHANGED) {
                result[i] = ops[i].getPosition();
            } else {
                result[i] = -1;
            }
        }
        return result;
    }
    @VisibleForTesting
    Token[] tokenize(final String seq) {
        int pos = 0;
        final int len = seq.length();
        final char[] chars = seq.toCharArray();
        Token[] tokens = new Token[len];
        int tokenCount = 0;
        while (pos < len) {
            while (pos < len && (chars[pos] == ' ' || chars[pos] == '\t')) {
                pos++;
            }
            int start = pos;
            while (pos < len && !(chars[pos] == ' ' || chars[pos] == '\t')) {
                pos++;
            }
            int end = pos;
            if (start != end) {
                tokens[tokenCount++] = new Token(chars, start, end);
            }
        }
        Token[] ret = new Token[tokenCount];
        System.arraycopy(tokens, 0, ret, 0, tokenCount);
        return ret;
    }
}
