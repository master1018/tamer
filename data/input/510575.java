public final class Matcher implements MatchResult {
    private Pattern pattern;
    private int nativePattern;
    private String input = "";
    private int regionStart;
    private int regionEnd;
    private boolean searching;
    private int findPos;
    private int appendPos;
    private boolean matchFound;
    private int[] matchOffsets;
    private boolean anchoringBounds = true;
    private boolean transparentBounds;
    Matcher(Pattern pattern, CharSequence input) {
        usePattern(pattern);
        reset(input);
    }
    public Matcher appendReplacement(StringBuffer buffer, String replacement) {
        buffer.append(input.substring(appendPos, start()));
        appendEvaluated(buffer, replacement);
        appendPos = end();
        return this;
    }
    private void appendEvaluated(StringBuffer buffer, String s) {
        boolean escape = false;
        boolean dollar = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar) {
                buffer.append(group(c - '0'));
                dollar = false;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
            }
        }
        if (escape) {
            throw new ArrayIndexOutOfBoundsException(s.length());
        }
    }
    private Matcher reset(CharSequence input, int start, int end) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        if (start < 0 || end < 0 || start > input.length() ||
                end > input.length() || start > end) {
            throw new IllegalArgumentException();
        }
        if (!input.equals(this.input)) {
            this.input = input.toString();
            NativeRegEx.setText(nativePattern, this.input);
            regionStart = 0;
            regionEnd = input.length();
        }
        if (start != regionStart || end != regionEnd) {
            regionStart = start;
            regionEnd = end;
            NativeRegEx.setRegion(nativePattern, regionStart, regionEnd);
        }
        searching = false;
        matchFound = false;
        findPos = regionStart;
        appendPos = 0;
        return this;
    }
    public Matcher reset(CharSequence input) {
        return reset(input, 0, input.length());
    }
    public Matcher reset() {
        return reset(input, 0, input.length());
    }
    public Matcher region(int start, int end) {
        return reset(input, start, end);
    }
    public StringBuffer appendTail(StringBuffer buffer) {
        if (appendPos < regionEnd) {
            buffer.append(input.substring(appendPos, regionEnd));
        }
        return buffer;
    }
    public String replaceFirst(String replacement) {
        StringBuffer buffer = new StringBuffer(input.length());
        findPos = 0;
        appendPos = 0;
        matchFound = false;
        searching = false;
        if (find()) {
            appendReplacement(buffer, replacement);
        }
        return appendTail(buffer).toString();
    }
    public String replaceAll(String replacement) {
        StringBuffer buffer = new StringBuffer(input.length());
        findPos = 0;
        appendPos = 0;
        matchFound = false;
        searching = false;
        while (find()) {
            appendReplacement(buffer, replacement);
        }
        return appendTail(buffer).toString();
    }
    public Pattern pattern() {
        return pattern;
    }
    public String group(int group) {
        ensureMatch();
        int from = matchOffsets[group * 2];
        int to = matchOffsets[(group * 2) + 1];
        if (from == -1 || to == -1) {
            return null;
        } else {
            return input.substring(from, to);
        }
    }
    public String group() {
        return group(0);
    }
    public boolean find(int start) {
        findPos = start;
        if (findPos < regionStart) {
            findPos = regionStart;
        } else if (findPos >= regionEnd) {
            matchFound = false;
            return false;
        }
        matchFound = NativeRegEx.find(nativePattern, findPos);
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        return matchFound;
    }
    public boolean find() {
        if (!searching) {
            searching = true;
            matchFound = NativeRegEx.find(nativePattern, -1);
        } else {
            matchFound = NativeRegEx.findNext(nativePattern);
        }
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        return matchFound;
    }
    public int start(int group) throws IllegalStateException {
        ensureMatch();
        return matchOffsets[group * 2];
    }
    public int end(int group) {
        ensureMatch();
        return matchOffsets[(group * 2) + 1];
    }
    public boolean matches() {
        matchFound = NativeRegEx.matches(nativePattern, -1); 
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        return matchFound;
    }
    public static String quoteReplacement(String s) {
        StringBuffer buffer = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                buffer.append('\\');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }
    public boolean lookingAt() {
        matchFound = NativeRegEx.lookingAt(nativePattern, -1); 
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        return matchFound;
    }
    public int start() {
        return start(0);
    }
    public int groupCount() {
        return pattern.mGroupCount;
    }
    public int end() {
        return end(0);
    }
    public MatchResult toMatchResult() {
        ensureMatch();
        return new MatchResultImpl(input, matchOffsets);
    }
    public Matcher useAnchoringBounds(boolean value) {
        anchoringBounds = value;
        NativeRegEx.useAnchoringBounds(nativePattern, value);
        return this;
    }
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }
    public Matcher useTransparentBounds(boolean value) {
        transparentBounds = value;
        NativeRegEx.useTransparentBounds(nativePattern, value);
        return this;
    }
    private void ensureMatch() {
        if (!matchFound) {
            throw new IllegalStateException("No successful match so far");
        }
    }
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }
    public int regionStart() {
        return regionStart;
    }
    public int regionEnd() {
        return regionEnd;
    }
    public boolean requireEnd() {
        return NativeRegEx.requireEnd(nativePattern);
    }
    public boolean hitEnd() {
        return NativeRegEx.hitEnd(nativePattern);
    }
    public Matcher usePattern(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        this.pattern = pattern;
        if (nativePattern != 0) {
            NativeRegEx.close(nativePattern);
        }
        nativePattern = NativeRegEx.clone(pattern.mNativePattern);
        if (input != null) {
            NativeRegEx.setText(nativePattern, input);
            NativeRegEx.setRegion(nativePattern, regionStart, regionEnd);
            NativeRegEx.useAnchoringBounds(nativePattern, anchoringBounds);
            NativeRegEx.useTransparentBounds(nativePattern, transparentBounds);
        }
        matchOffsets = new int[(this.pattern.mGroupCount + 1) * 2];
        matchFound = false;
        return this;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (nativePattern != 0) {
                NativeRegEx.close(nativePattern);
            }
        }
        finally {
            super.finalize();
        }
    }
}
